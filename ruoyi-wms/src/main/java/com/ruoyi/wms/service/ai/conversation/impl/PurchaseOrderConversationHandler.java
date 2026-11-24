package com.ruoyi.wms.service.ai.conversation.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.wms.constant.WmsApproveConstants;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsGoodsCategory;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.WmsUnit;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.ai.chat.ChatRequestVO;
import com.ruoyi.wms.domain.vo.ai.chat.ChatResponseVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.service.WmsGoodsCategoryService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import com.ruoyi.wms.service.WmsSupplierService;
import com.ruoyi.wms.service.WmsUnitService;
import com.ruoyi.wms.service.WmsWarehouseService;
import com.ruoyi.wms.service.ai.conversation.ConversationHandler;
import com.ruoyi.wms.service.ai.conversation.ConversationState;
import com.ruoyi.wms.service.ai.parser.ParsedOrderResult;
import com.ruoyi.wms.service.ai.parser.SmartOrderParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 采购订单对话处理器
 *
 * 完整对话流程：
 * 1. 供应商（不存在则引导创建）→ 2. 仓库 → 3. 商品（可循环添加，不存在则引导创建SKU）→ 4. 订单其他信息 → 5. 确认 → 完成
 *
 * @author ruoyi
 */
@Component
public class PurchaseOrderConversationHandler implements ConversationHandler {

	private static final Logger log = LoggerFactory.getLogger(PurchaseOrderConversationHandler.class);

	@Autowired
	private WmsSupplierService supplierService;

	@Autowired
	private WmsGoodsSkuService goodsSkuService;

	@Autowired
	private WmsGoodsService goodsService;

	@Autowired
	private WmsGoodsCategoryService goodsCategoryService;

	@Autowired
	private WmsUnitService unitService;

	@Autowired
	private WmsWarehouseService warehouseService;

	@Autowired
	private com.ruoyi.wms.service.WmsPurchaseInService purchaseOrderService;

	@Autowired
	private WmsBillApproveFlowService approveFlowService;

	@Autowired
	private ObjectMapper objectMapper;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	// 对话步骤枚举
	private static final String STEP_SUPPLIER = "supplier";
	private static final String STEP_SUPPLIER_SELECT = "supplier_select";
	private static final String STEP_SUPPLIER_LIST_SELECT = "supplier_list_select";
	private static final String STEP_SUPPLIER_NEW_CONFIRM = "supplier_new_confirm";
	private static final String STEP_SUPPLIER_NEW = "supplier_new";
	private static final String STEP_SUPPLIER_CONFIRM = "supplier_confirm";
	private static final String STEP_SUPPLIER_CREATE_PAGE = "supplier_create_page";
	private static final String STEP_WAREHOUSE = "warehouse";
	private static final String STEP_WAREHOUSE_NEW = "warehouse_new";
	private static final String STEP_GOODS = "goods";
	private static final String STEP_GOODS_NEW = "goods_new";
	private static final String STEP_GOODS_NEW_CONFIRM = "goods_new_confirm";
	private static final String STEP_GOODS_CREATE_CONFIRM = "goods_create_confirm";
	private static final String STEP_GOODS_CREATE_PAGE = "goods_create_page";
	private static final String STEP_GOODS_CONFIRM = "goods_confirm";
	private static final String STEP_GOODS_QUANTITY = "goods_quantity";
	private static final String STEP_GOODS_PRICE = "goods_price";
	private static final String STEP_MORE_GOODS = "more_goods";
	private static final String STEP_ORDER_INFO = "order_info";
	private static final String STEP_CONFIRM_ORDER = "confirm_order";

	@Override
	public String getOperationType() {
		return "create_purchase_in";
	}

	@Override
	public ChatResponseVO start(ConversationState state) {
		state.setCurrentStep(STEP_GOODS);
		state.setData("items", new ArrayList<Map<String, Object>>());

		// 检查是否有仓库，优先默认仓库
		List<WmsWarehouse> warehouses = warehouseService.list(
				Wrappers.<WmsWarehouse>lambdaQuery()
						.orderByDesc(WmsWarehouse::getIsDefault)
						.orderByDesc(WmsWarehouse::getCreateTime)
		);
		if (warehouses != null && !warehouses.isEmpty()) {
			state.setData("availableWarehouses", warehouses);
			state.setData("warehouseId", warehouses.get(0).getId());
			state.setData("warehouseName", warehouses.get(0).getWarehouseName());
		}

		// 获取常用供应商列表，供后续使用
		List<WmsSupplier> suppliers = supplierService.list(
				Wrappers.<WmsSupplier>lambdaQuery()
						.orderByDesc(WmsSupplier::getCreateTime)
						.last("LIMIT 3")
		);
		if (suppliers != null && !suppliers.isEmpty()) {
			state.setData("availableSuppliers", suppliers);
		}

		StringBuilder reply = new StringBuilder();
		reply.append("好的，我来帮您创建**采购入库单**。\n\n");
		reply.append("💡 系统会自动识别，找不到的会帮您新建\n");
		reply.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

		// 示例提示词列表
		List<String> examplePrompts = Arrays.asList(
			"向云腾科技有限公司采购苹果笔记本50台，单价是8000元。",
			"从华联供应商进货100箱可乐，单价50元。",
			"跟京东买200个无线鼠标，单价25元。"
		);

		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(reply.toString())
				.needMoreInfo(true)
				.examplePrompts(examplePrompts)
				.build();
	}

	@Override
	public ChatResponseVO handle(ConversationState state, ChatRequestVO request) {
		String step = state.getCurrentStep();
		String message = request.getMessage().trim();

		log.info("采购入库对话 - 步骤: {}, 输入: {}", step, message);

		switch (step) {
			case STEP_SUPPLIER:
				return handleSupplier(state, message);
			case STEP_SUPPLIER_SELECT:
				return handleSupplierSelect(state, message);
			case STEP_SUPPLIER_LIST_SELECT:
				return handleSupplierListSelect(state, message);
			case STEP_SUPPLIER_NEW_CONFIRM:
				return handleSupplierNewConfirm(state, message);
			case STEP_SUPPLIER_NEW:
				return handleSupplierNew(state, message);
			case STEP_SUPPLIER_CONFIRM:
				return handleSupplierConfirm(state, message);
			case STEP_SUPPLIER_CREATE_PAGE:
				return handleSupplierCreatePage(state, message);
			case STEP_WAREHOUSE:
				return handleWarehouse(state, message);
			case STEP_GOODS:
				return handleGoods(state, message);
			case STEP_GOODS_SELECT:
				return handleGoodsSelect(state, message);
			case STEP_GOODS_NEW:
				return handleGoodsNew(state, message);
			case STEP_GOODS_NEW_CONFIRM:
				return handleGoodsNewConfirm(state, message);
			case STEP_GOODS_CREATE_CONFIRM:
				return handleGoodsCreateConfirm(state, message);
			case STEP_GOODS_CREATE_PAGE:
				return handleGoodsCreatePage(state, message);
			case STEP_GOODS_CONFIRM:
				return handleGoodsConfirm(state, message);
			case STEP_GOODS_QUANTITY:
				return handleGoodsQuantity(state, message);
			case STEP_GOODS_PRICE:
				return handleGoodsPrice(state, message);
			case STEP_MORE_GOODS:
				return handleMoreGoods(state, message);
			case STEP_ORDER_INFO:
				return handleOrderInfo(state, message);
			case STEP_CONFIRM_ORDER:
				return handleConfirmOrder(state, message);
			default:
				return start(state);
		}
	}

	// ==================== 供应商相关 ====================

	private ChatResponseVO askForSupplier(ConversationState state) {
		List<WmsSupplier> suppliers = (List<WmsSupplier>) state.getData("availableSuppliers");
		StringBuilder sb = new StringBuilder();
		sb.append("**请选择或输入供应商**：\n");

		if (suppliers != null && !suppliers.isEmpty()) {
			sb.append("\n📋 常用供应商（输入关键字即可）：\n");
			for (int i = 0; i < suppliers.size(); i++) {
				WmsSupplier s = suppliers.get(i);
				sb.append("\n").append(i + 1).append(". ").append(s.getSupplierName());
			}
		} else {
			sb.append("\n💡 直接输入供应商名称，系统会自动创建");
		}

		return response(state, sb.toString());
	}

	private ChatResponseVO handleSupplier(ConversationState state, String message) {
		// 检查用户是否在确认新建供应商
		if (state.hasData("tempSupplierName")) {
			if (isYes(message)) {
				// 直接记录新供应商
				state.setData("supplierId", null);
				state.setData("supplierName", state.getData("tempSupplierName"));
				state.setData("isNewSupplier", true);
				state.removeData("tempSupplierName");
				state.setCurrentStep(STEP_WAREHOUSE);

				return response(state, "✅ 供应商【" + state.getData("supplierName") + "】已记录。\n\n" + askForWarehouse(state).getReply());
			} else if (isCancel(message)) {
				// 用户不想创建，清除临时数据并重新显示供应商列表
				state.removeData("tempSupplierName");
				return responseWithSupplierList(state, "请重新选择**供应商**（输入序号或供应商名称）：");
			}
		}

		// 查找供应商
		WmsSupplier supplier = findSupplier(message);

		if (supplier != null) {
			// 找到供应商，先保存，让用户确认
			state.setData("tempSelectedSupplierId", supplier.getId());
			state.setData("tempSelectedSupplierName", supplier.getSupplierName());
			state.setCurrentStep(STEP_SUPPLIER_SELECT);
			return response(state, "找到供应商：**" + supplier.getSupplierName() + "**\n\n" +
					"请确认：\n" +
					"- 回答\"是\"或\"确认\"选择此供应商\n" +
					"- 重新输入其他供应商名称").withYesNoReplies();
		}

		// 没找到供应商，询问是否新建
		state.setData("tempSupplierName", message);
		return response(state, "⚠️ 没有找到供应商【" + message + "】\n\n" +
				"这是一个**新供应商**吗？\n" +
				"- 回答\"是\"，我将记录这个供应商\n" +
				"- 回答\"否\"，重新选择供应商").withYesNoReplies();
	}

	private ChatResponseVO handleSupplierSelect(ConversationState state, String message) {
		// 用户确认选择此供应商
		if (isYes(message) || isConfirm(message)) {
			state.setData("supplierId", state.getData("tempSelectedSupplierId"));
			state.setData("supplierName", state.getData("tempSelectedSupplierName"));
			state.removeData("tempSelectedSupplierId");
			state.removeData("tempSelectedSupplierName");
			state.setCurrentStep(STEP_ORDER_INFO);

			return response(state, "✅ 已选择供应商：" + state.getData("supplierName") +
					"\n\n" + askOrderInfo(state).getReply());
		}

		// 用户不想选择此供应商，重新搜索
		state.removeData("tempSelectedSupplierId");
		state.removeData("tempSelectedSupplierName");
		state.setCurrentStep(STEP_SUPPLIER);
		return responseWithSupplierList(state, "请重新选择**供应商**（输入序号或供应商名称）：");
	}

	/**
	 * 处理供应商列表选择（多个匹配时）
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO handleSupplierListSelect(ConversationState state, String message) {
		List<WmsSupplier> supplierList = (List<WmsSupplier>) state.getData("tempMatchedSupplierList");

		// 检查是否选择创建新供应商
		if ("创建新供应商".equals(cleanPunctuation(message))) {
			String supplierName = (String) state.getData("tempSupplierInput");
			state.setData("supplierId", null);
			state.setData("supplierName", supplierName);
			state.setData("isNewSupplier", true);
			state.removeData("tempMatchedSupplierList");
			state.removeData("tempSupplierInput");
			state.removeData("tempGoodsInfoForSupplier");

			// 继续显示订单确认页面
			state.setCurrentStep(STEP_CONFIRM_ORDER);
			return showOrderConfirm(state);
		}

		// 尝试解析选择的序号
		try {
			int index = Integer.parseInt(cleanPunctuation(message)) - 1;
			if (supplierList != null && index >= 0 && index < supplierList.size()) {
				WmsSupplier selectedSupplier = supplierList.get(index);
				state.setData("supplierId", selectedSupplier.getId());
				state.setData("supplierName", selectedSupplier.getSupplierName());
				state.setData("isNewSupplier", false);
				state.removeData("tempMatchedSupplierList");
				state.removeData("tempSupplierInput");
				state.removeData("tempGoodsInfoForSupplier");

				// 继续显示订单确认页面
				state.setCurrentStep(STEP_CONFIRM_ORDER);
				return showOrderConfirm(state);
			}
		} catch (NumberFormatException e) {
			// 不是数字，继续处理
		}

		// 输入无效，重新显示选择列表
		if (supplierList != null && !supplierList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("⚠️ 请输入有效的序号（1-").append(supplierList.size()).append("）\n\n");
			sb.append("🔍 匹配的供应商：\n");
			sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

			for (int i = 0; i < supplierList.size(); i++) {
				WmsSupplier s = supplierList.get(i);
				sb.append("\n**").append(i + 1).append(".** ").append(s.getSupplierName());
				if (s.getContact() != null && !s.getContact().isEmpty()) {
					sb.append("（联系人：").append(s.getContact()).append("）");
				}
				sb.append("\n");
			}

			sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
			sb.append("请输入序号选择，或者说\"创建新供应商\"");

			return response(state, sb.toString());
		}

		// 出错，返回商品输入步骤
		state.setCurrentStep(STEP_GOODS);
		return response(state, "请重新输入**商品信息**？\n\n示例：\"5台iPhone，8000元，苹果公司\"");
	}

	private ChatResponseVO handleSupplierNewConfirm(ConversationState state, String message) {
		// 用户已经确认创建新供应商
		state.setData("supplierId", null);
		state.setData("supplierName", state.getData("tempSupplierName"));
		state.setData("isNewSupplier", true);
		state.removeData("tempSupplierName");
		state.setCurrentStep(STEP_WAREHOUSE);

		return response(state, "✅ 供应商【" + state.getData("supplierName") + "】已记录。\n\n" + askForWarehouse(state).getReply());
	}

	private ChatResponseVO askSupplierInfo(ConversationState state) {
		state.setCurrentStep(STEP_WAREHOUSE);
		return response(state, "✅ 供应商【" + state.getData("tempSupplierName") + "】已记录。\n\n" + askForWarehouse(state).getReply());
	}

	private ChatResponseVO handleSupplierNew(ConversationState state, String message) {
		state.setData("supplierId", null);
		state.setData("supplierName", state.getData("tempSupplierName"));
		state.setData("isNewSupplier", true);
		state.removeData("tempSupplierName");
		state.setCurrentStep(STEP_WAREHOUSE);

		return response(state, "✅ 供应商【" + state.getData("supplierName") + "】已记录。\n\n" + askForWarehouse(state).getReply());
	}

	private ChatResponseVO showSupplierConfirm(ConversationState state) {
		return null; // 不再需要确认步骤
	}

	private ChatResponseVO handleSupplierConfirm(ConversationState state, String message) {
		return null; // 不再需要确认步骤
	}

	/**
	 * 处理从供应商创建页面返回
	 * 用户创建完供应商后，继续生成订单预览
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO handleSupplierCreatePage(ConversationState state, String message) {
		// 用户创建完成，重新查找供应商
		String supplierName = (String) state.getData("tempSupplierName");
		if (supplierName == null) {
			supplierName = message;
		}

		// 重新查找供应商
		List<WmsSupplier> supplierList = findSupplierList(supplierName);
		if (supplierList != null && !supplierList.isEmpty()) {
			WmsSupplier supplier = supplierList.get(0);
			state.setData("supplierId", supplier.getId());
			state.setData("supplierName", supplier.getSupplierName());
			state.setData("isNewSupplier", false);
		} else {
			state.setData("supplierId", null);
			state.setData("supplierName", supplierName);
			state.setData("isNewSupplier", true);
		}

		state.removeData("tempSupplierName");

		// 显示订单确认页面
		state.setCurrentStep(STEP_CONFIRM_ORDER);
		return showOrderConfirm(state);
	}

	// ==================== 仓库相关 ====================

	private ChatResponseVO askForWarehouse(ConversationState state) {
		// 采购订单不需要选择仓库，直接跳到订单信息
		List<WmsWarehouse> warehouses = (List<WmsWarehouse>) state.getData("availableWarehouses");
		if (warehouses != null && !warehouses.isEmpty()) {
			state.setData("warehouseId", warehouses.get(0).getId());
			state.setData("warehouseName", warehouses.get(0).getWarehouseName());
		}
		return askOrderInfo(state);
	}

	private ChatResponseVO handleWarehouse(ConversationState state, String message) {
		// 采购订单不需要选择仓库，直接跳到订单信息
		return askForWarehouse(state);
	}

	// ==================== 商品相关 ====================

	/**
	 * 处理商品输入（一句话模式）
	 * 用户输入格式：商品 + 数量 + 单价 + 供应商
	 * 示例："5台IPHONE8000元，苹果公司"
	 */
	private ChatResponseVO handleGoods(ConversationState state, String message) {
		if (isCancel(message)) {
			return reviewItems(state);
		}

		// 解析商品信息（包含商品名称、数量、单价、供应商）
		Map<String, Object> goodsInfo = parseGoodsInput(message);

		log.info("解析商品输入: 原始={}, 解析结果={}", message, goodsInfo);

		// 检查解析错误（商品名称/供应商/数量/价格缺失）
		String errorMsg = (String) goodsInfo.get("error");
		if (errorMsg != null && !errorMsg.isEmpty()) {
			// 构建快捷回复选项：重新创建
			List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
			quickReplies.add(new ChatResponseVO.QuickReply("重新创建", "重新创建采购入库", "primary"));

			return response(state, "❌ **创建订单失败**\n\n" + errorMsg + "\n\n请点击下方按钮或输入新的指令重新开始。")
					.withQuickReplies(quickReplies);
		}

		String goodsName = (String) goodsInfo.get("name");

		// 查找商品列表（返回最新3条）
		List<WmsGoodsSkuRespVO> skuList = findGoodsList(goodsName);

		if (skuList != null && !skuList.isEmpty()) {
			// 找到商品，处理商品匹配
			return handleGoodsMatch(state, goodsInfo, skuList);
		}

		// 没找到商品，进入快速创建/填写详情/取消闭环
		return handleGoodsNotFound(state, goodsInfo, message);
	}

	private ChatResponseVO handleGoodsNotFound(ConversationState state, Map<String, Object> goodsInfo, String originalMessage) {
		String parsedGoodsName = (String) goodsInfo.get("name");
		String cleanedName = cleanGoodsName(parsedGoodsName);

		// 二次纠错后重查，避免动词残留导致误判
		if (!cleanedName.equals(parsedGoodsName)) {
			goodsInfo.put("name", cleanedName);
			List<WmsGoodsSkuRespVO> retrySkuList = findGoodsList(cleanedName);
			if (retrySkuList != null && !retrySkuList.isEmpty()) {
				return handleGoodsMatch(state, goodsInfo, retrySkuList);
			}
		}

		Map<String, Object> orderContext = new HashMap<>();
		orderContext.put("quantity", goodsInfo.get("quantity"));
		orderContext.put("price", goodsInfo.get("price"));
		orderContext.put("unit", goodsInfo.get("unit"));
		orderContext.put("supplier", goodsInfo.get("supplier"));
		orderContext.put("originalMessage", originalMessage);
		state.setData("tempOrderContext", orderContext);
		state.setData("tempGoodsInfoForCreate", goodsInfo);
		state.setData("tempGoodsName", cleanedName);
		state.setCurrentStep(STEP_GOODS_CREATE_CONFIRM);

		StringBuilder sb = new StringBuilder();
		sb.append("⚠️ 未找到商品【").append(cleanedName).append("】\n\n");
		sb.append("请选择后续操作：\n");
		sb.append("• 快速创建：打开创建页并预填已识别信息\n");
		sb.append("• 填写详情：前往表单完整填写\n");
		sb.append("• 取消：返回重新输入商品信息");

		List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
		quickReplies.add(new ChatResponseVO.QuickReply("快速创建", "快速创建", "primary"));
		quickReplies.add(new ChatResponseVO.QuickReply("填写详情", "填写详情", "default"));
		quickReplies.add(new ChatResponseVO.QuickReply("取消", "取消", "default"));
		return response(state, sb.toString()).withQuickReplies(quickReplies);
	}

	@SuppressWarnings("unchecked")
	private ChatResponseVO handleGoodsCreateConfirm(ConversationState state, String message) {
		String cleanMessage = cleanPunctuation(message);
		Map<String, Object> goodsInfo = (Map<String, Object>) state.getData("tempGoodsInfoForCreate");
		String goodsName = goodsInfo != null ? (String) goodsInfo.get("name") : state.getData("tempGoodsName", "");

		if ("取消".equals(cleanMessage) || "重新输入".equals(cleanMessage) || isCancel(cleanMessage)) {
			state.removeData("tempGoodsInfoForCreate");
			state.removeData("tempOrderContext");
			state.removeData("tempGoodsName");
			state.setCurrentStep(STEP_GOODS);
			return response(state, "好的，请重新告诉我商品信息。\n\n示例：\"5台iPhone，8000元，苹果公司\"");
		}

		if ("快速创建".equals(cleanMessage) || "快速".equals(cleanMessage)) {
			return handleQuickCreateAndContinue(state, goodsInfo);
		}

		if ("填写详情".equals(cleanMessage) || "详情".equals(cleanMessage)) {
			return showGoodsCreatePage(state, goodsName, goodsInfo, "detail");
		}

		return handleUnexpectedInput(state, message);
	}

	private ChatResponseVO handleQuickCreateAndContinue(ConversationState state, Map<String, Object> goodsInfo) {
		if (goodsInfo == null) {
			return response(state, "快速创建失败：缺少商品信息，请重新输入。\n\n示例：\"采购3台苹果笔记本，单价8000元\"");
		}

		String goodsName = nvl((String) goodsInfo.get("name"), "").trim();
		if (goodsName.isEmpty()) {
			return response(state, "快速创建失败：未识别到商品名称，请重新输入。");
		}

		try {
			String categoryId = resolveDefaultCategoryId();
			if (categoryId == null) {
				return response(state, "快速创建失败：系统未配置可用商品分类，请先维护分类后重试。");
			}

			WmsGoodsSaveReqVO goodsReq = new WmsGoodsSaveReqVO();
			goodsReq.setGoodsName(goodsName);
			goodsReq.setCategoryId(categoryId);
			goodsReq.setHasSpec("0");
			goodsReq.setStatus("0");
			goodsReq.setRemark("AI助手快速创建");

			Long goodsId = goodsService.create(goodsReq);

			BigDecimal purchasePrice = null;
			Object priceObj = goodsInfo.get("price");
			if (priceObj instanceof BigDecimal) {
				purchasePrice = (BigDecimal) priceObj;
			} else if (priceObj != null) {
				try {
					purchasePrice = new BigDecimal(priceObj.toString());
				} catch (Exception ignored) {
					purchasePrice = null;
				}
			}

			String unitName = goodsInfo.get("unit") == null ? "件" : goodsInfo.get("unit").toString();
			Long unitId = resolveUnitIdByName(unitName);

			WmsGoodsSkuSaveReqVO skuReq = new WmsGoodsSkuSaveReqVO();
			skuReq.setGoodsId(goodsId);
			skuReq.setSkuName(goodsName);
			skuReq.setSpecInfo("默认规格");
			skuReq.setUnitId(unitId);
			skuReq.setPurchasePrice(purchasePrice);
			skuReq.setSalePrice(purchasePrice);
			skuReq.setStatus("0");

			Long skuId = goodsSkuService.create(skuReq);
			return continueOrderWithNewGoods(state, goodsId, skuId, goodsName);
		} catch (Exception e) {
			log.error("快速创建商品失败, goodsInfo={}", goodsInfo, e);
			return response(state, "快速创建失败：" + nvl(e.getMessage(), "未知错误") + "\n\n您可以点击\"填写详情\"继续创建。")
					.withQuickReplies(Arrays.asList(
							new ChatResponseVO.QuickReply("填写详情", "填写详情", "primary"),
							new ChatResponseVO.QuickReply("取消", "取消", "default")
					));
		}
	}

	private String resolveDefaultCategoryId() {
		List<WmsGoodsCategory> categories = goodsCategoryService.list(
				Wrappers.<WmsGoodsCategory>lambdaQuery()
						.eq(WmsGoodsCategory::getStatus, "0")
						.orderByDesc(WmsGoodsCategory::getCreateTime)
						.last("LIMIT 1")
		);
		if (categories == null || categories.isEmpty()) {
			return null;
		}
		return String.valueOf(categories.get(0).getId());
	}

	private Long resolveUnitIdByName(String unitName) {
		if (unitName != null && !unitName.trim().isEmpty()) {
			List<WmsUnit> exactUnits = unitService.list(
					Wrappers.<WmsUnit>lambdaQuery()
							.eq(WmsUnit::getStatus, "0")
							.eq(WmsUnit::getUnitName, unitName.trim())
							.orderByDesc(WmsUnit::getCreateTime)
							.last("LIMIT 1")
			);
			if (exactUnits != null && !exactUnits.isEmpty()) {
				return exactUnits.get(0).getId();
			}
		}

		List<WmsUnit> defaultUnits = unitService.list(
				Wrappers.<WmsUnit>lambdaQuery()
						.eq(WmsUnit::getStatus, "0")
						.eq(WmsUnit::getIsDefault, 1)
						.orderByDesc(WmsUnit::getCreateTime)
						.last("LIMIT 1")
		);
		if (defaultUnits != null && !defaultUnits.isEmpty()) {
			return defaultUnits.get(0).getId();
		}

		List<WmsUnit> units = unitService.list(
				Wrappers.<WmsUnit>lambdaQuery()
						.eq(WmsUnit::getStatus, "0")
						.orderByDesc(WmsUnit::getCreateTime)
						.last("LIMIT 1")
		);
		if (units != null && !units.isEmpty()) {
			return units.get(0).getId();
		}
		return null;
	}

	private ChatResponseVO handleUnexpectedInput(ConversationState state, String message) {
		log.info("商品创建确认步骤收到非预期输入: {}", message);
		return response(state,
				"⚠️ 当前步骤仅支持以下操作：\n\n" +
				"• 快速创建\n" +
				"• 填写详情\n" +
				"• 取消\n\n" +
				"请点击按钮，或直接输入上述任一选项。");
	}

	private ChatResponseVO showGoodsCreatePage(ConversationState state, String goodsName, Map<String, Object> goodsInfo, String createMode) {
		state.setData("tempGoodsName", goodsName);
		state.setCurrentStep(STEP_GOODS_CREATE_PAGE);

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("goodsName", nvl(goodsName, ""));
		arguments.put("fromChat", "true");
		arguments.put("conversationId", state.getConversationId());
		arguments.put("createMode", nvl(createMode, "detail"));
		if (goodsInfo != null) {
			if (goodsInfo.get("unit") != null) {
				arguments.put("unit", goodsInfo.get("unit"));
				arguments.put("unitName", goodsInfo.get("unit"));
			}
			if (goodsInfo.get("price") != null) {
				arguments.put("purchasePrice", goodsInfo.get("price"));
			}
		}

		ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
		action.setName("create_goods");
		action.setDescription("前往商品创建页面");
		action.setArguments(arguments);

		String reply = "quick".equals(createMode)
				? "好的，进入快速创建页面。\n\n- 已自动带入商品名/单位/采购价\n- 保存成功后将自动返回继续对话"
				: "好的，请前往商品创建页面。\n\n- 已自动带入可识别的信息\n- 创建完成后将自动返回继续对话";

		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(reply)
				.suggestedAction(action)
				.collectedData(state.getCollectedData())
				.needMoreInfo(true)
				.build();
	}

	/**
	 * 处理商品匹配（找到商品后）
	 */
	private ChatResponseVO handleGoodsMatch(ConversationState state, Map<String, Object> goodsInfo, List<WmsGoodsSkuRespVO> skuList) {

		if (skuList.size() == 1) {
			// 只有一个匹配，直接使用
			WmsGoodsSkuRespVO sku = skuList.get(0);
			return createOrderPreviewWithExistingGoods(state, goodsInfo, sku);
		} else {
			// 多个匹配，让用户选择
			state.setData("tempGoodsInfo", goodsInfo);
			state.setData("tempMatchedGoodsList", skuList);
			state.setCurrentStep(STEP_GOODS_SELECT);

			StringBuilder sb = new StringBuilder();
			sb.append("🔍 找到 ").append(skuList.size()).append(" 个匹配的商品：\n");
			sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

			for (int i = 0; i < skuList.size(); i++) {
				WmsGoodsSkuRespVO sku = skuList.get(i);
				sb.append("\n**").append(i + 1).append(".** ").append(sku.getSkuName());
				if (sku.getSpecInfo() != null && !sku.getSpecInfo().isEmpty()) {
					sb.append(" (").append(sku.getSpecInfo()).append(")");
				}
				if (sku.getPurchasePrice() != null) {
					sb.append(" - 参考价：¥").append(sku.getPurchasePrice());
				}
				sb.append("\n");
			}

			sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
			sb.append("请输入序号选择，或者说\"创建新商品\"");

			// 构建快捷回复选项
			List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
			for (int i = 0; i < skuList.size(); i++) {
				quickReplies.add(new ChatResponseVO.QuickReply((i + 1) + "", String.valueOf(i + 1), "default"));
			}
			quickReplies.add(new ChatResponseVO.QuickReply("创建新商品", "创建新商品", "primary"));

			return response(state, sb.toString()).withQuickReplies(quickReplies);
		}
	}

	/**
	 * 使用已有商品创建订单预览
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO createOrderPreviewWithExistingGoods(ConversationState state, Map<String, Object> goodsInfo, WmsGoodsSkuRespVO sku) {
		Integer quantity = (Integer) goodsInfo.get("quantity");
		BigDecimal price = (BigDecimal) goodsInfo.get("price");
		String supplier = (String) goodsInfo.get("supplier");
		String unit = (String) goodsInfo.get("unit");

		// 如果没有数量，使用默认值1
		if (quantity == null) {
			quantity = 1;
		}

		// 如果没有价格，使用商品的采购价
		if (price == null && sku.getPurchasePrice() != null) {
			price = sku.getPurchasePrice();
		}

		// 如果没有单位，使用商品的单位
		if (unit == null || unit.isEmpty()) {
			unit = sku.getUnitName() != null ? sku.getUnitName() : "件";
		}

		// 计算金额
		BigDecimal amount = price != null ? price.multiply(new BigDecimal(quantity)) : BigDecimal.ZERO;

		// 构建商品明细
		Map<String, Object> item = new HashMap<>();
		item.put("skuId", sku.getId());
		item.put("goodsId", sku.getGoodsId());
		item.put("name", sku.getSkuName());
		item.put("spec", sku.getSpecInfo());
		item.put("unit", unit);
		item.put("quantity", quantity);
		item.put("price", price);
		item.put("amount", amount);
		item.put("isNew", false);

		// 添加到商品列表
		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
		if (items == null) {
			items = new ArrayList<>();
		}
		items.add(item);
		state.setData("items", items);

		// 设置默认仓库
		List<WmsWarehouse> warehouses = (List<WmsWarehouse>) state.getData("availableWarehouses");
		if (warehouses != null && !warehouses.isEmpty()) {
			state.setData("warehouseId", warehouses.get(0).getId());
			state.setData("warehouseName", warehouses.get(0).getWarehouseName());
		}

		// 处理供应商 - 支持多供应商选择
		if (supplier != null && !supplier.isEmpty()) {
			resolveSupplierFast(state, supplier);
		}

		return continueToConfirmOrAskSupplier(state);
	}

	/**
	 * 使用新商品创建订单预览
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO createOrderPreviewWithNewGoods(ConversationState state, Map<String, Object> goodsInfo) {
		String goodsName = (String) goodsInfo.get("name");
		Integer quantity = (Integer) goodsInfo.get("quantity");
		BigDecimal price = (BigDecimal) goodsInfo.get("price");
		String supplier = (String) goodsInfo.get("supplier");
		String unit = (String) goodsInfo.get("unit");

		// 如果没有数量，使用默认值1
		if (quantity == null) {
			quantity = 1;
		}

		// 如果没有单位，使用默认单位"件"
		if (unit == null || unit.isEmpty()) {
			unit = "件";
		}

		// 计算金额
		BigDecimal amount = price != null ? price.multiply(new BigDecimal(quantity)) : BigDecimal.ZERO;

		// 构建商品明细
		Map<String, Object> item = new HashMap<>();
		item.put("skuId", null);
		item.put("goodsId", null);
		item.put("name", goodsName);
		item.put("spec", "");
		item.put("unit", unit);
		item.put("quantity", quantity);
		item.put("price", price);
		item.put("amount", amount);
		item.put("isNew", true);

		// 添加到商品列表
		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
		if (items == null) {
			items = new ArrayList<>();
		}
		items.add(item);
		state.setData("items", items);

		// 设置默认仓库
		List<WmsWarehouse> warehouses = (List<WmsWarehouse>) state.getData("availableWarehouses");
		if (warehouses != null && !warehouses.isEmpty()) {
			state.setData("warehouseId", warehouses.get(0).getId());
			state.setData("warehouseName", warehouses.get(0).getWarehouseName());
		}

		// 处理供应商 - 支持多供应商选择
		if (supplier != null && !supplier.isEmpty()) {
			resolveSupplierFast(state, supplier);
		}

		return continueToConfirmOrAskSupplier(state);
	}

	// 商品选择步骤
	private static final String STEP_GOODS_SELECT = "goods_select";

	/**
	 * 处理商品选择
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO handleGoodsSelect(ConversationState state, String message) {
		List<WmsGoodsSkuRespVO> skuList = (List<WmsGoodsSkuRespVO>) state.getData("tempMatchedGoodsList");
		Map<String, Object> goodsInfo = (Map<String, Object>) state.getData("tempGoodsInfo");

		// 检查是否选择创建新商品
		if ("创建新商品".equals(cleanPunctuation(message))) {
			state.removeData("tempMatchedGoodsList");
			state.removeData("tempGoodsInfo");
			return createOrderPreviewWithNewGoods(state, goodsInfo);
		}

		// 尝试解析选择的序号
		try {
			int index = Integer.parseInt(cleanPunctuation(message)) - 1;
			if (skuList != null && index >= 0 && index < skuList.size()) {
				WmsGoodsSkuRespVO selectedSku = skuList.get(index);
				state.removeData("tempMatchedGoodsList");
				state.removeData("tempGoodsInfo");
				return createOrderPreviewWithExistingGoods(state, goodsInfo, selectedSku);
			}
		} catch (NumberFormatException e) {
			// 不是数字，继续处理
		}

		// 输入无效，重新显示选择列表
		if (skuList != null && !skuList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("⚠️ 请输入有效的序号（1-").append(skuList.size()).append("）\n\n");
			sb.append("🔍 匹配的商品：\n");
			sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

			for (int i = 0; i < skuList.size(); i++) {
				WmsGoodsSkuRespVO sku = skuList.get(i);
				sb.append("\n**").append(i + 1).append(".** ").append(sku.getSkuName());
				if (sku.getSpecInfo() != null && !sku.getSpecInfo().isEmpty()) {
					sb.append(" (").append(sku.getSpecInfo()).append(")");
				}
				sb.append("\n");
			}

			sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
			sb.append("请输入序号选择，或者说\"创建新商品\"");

			return response(state, sb.toString());
		}

		// 出错，返回商品输入步骤
		state.setCurrentStep(STEP_GOODS);
		return response(state, "请重新输入**商品信息**？\n\n示例：\"5台iPhone，8000元，苹果公司\"");
	}

	private ChatResponseVO askGoodsInfo(ConversationState state) {
		return response(state, "好的，请提供商品的基本信息：\n\n" +
				"📝 **请一次性告诉我**（可以按任意格式）：\n\n" +
				"**必填：**\n" +
				"- 规格（如：500ml/瓶）\n" +
				"- 单位（如：瓶、箱）\n\n" +
				"**选填：**\n" +
				"- 条形码\n" +
				"- 采购价\n" +
				"- 销售价\n\n" +
				"📎 示例：500ml/瓶 瓶 6901234567890 45 60");
	}

	/**
	 * 处理商品创建页面相关操作
	 */
	private ChatResponseVO handleGoodsCreatePage(ConversationState state, String message) {
		String cleanMessage = cleanPunctuation(message);

		if (message != null && message.startsWith("goods_created:")) {
			Map<String, Object> createdGoods = parseCreatedGoodsPayload(message.substring("goods_created:".length()));
			Long goodsId = toLong(createdGoods.get("goodsId"));
			Long skuId = toLong(createdGoods.get("skuId"));
			String goodsName = createdGoods.get("goodsName") == null ? state.getData("tempGoodsName", "") : createdGoods.get("goodsName").toString();
			return continueOrderWithNewGoods(state, goodsId, skuId, goodsName);
		}

		if (message != null && message.startsWith("goods_create_failed:")) {
			String errorMsg = message.substring("goods_create_failed:".length()).trim();
			state.setCurrentStep(STEP_GOODS);
			state.removeData("tempGoodsName");
			state.removeData("tempGoodsInfoForCreate");
			state.removeData("tempOrderContext");
			return response(state, "❌ 商品创建失败：" + nvl(errorMsg, "未知错误") + "\n\n请重新输入商品信息。");
		}

		if ("商品创建完成".equals(cleanMessage)) {
			// 用户从商品创建页面返回
			String goodsName = state.getData("tempGoodsName", "");

			if (state.hasData("tempOrderContext")) {
				Long goodsId = null;
				Long skuId = null;
				if (!goodsName.isEmpty()) {
					WmsGoodsSkuRespVO sku = findGoods(goodsName);
					if (sku != null) {
						goodsId = sku.getGoodsId();
						skuId = sku.getId();
						goodsName = sku.getSkuName();
					}
				}
				return continueOrderWithNewGoods(state, goodsId, skuId, goodsName);
			}

			state.removeData("tempGoodsName");

			if (!goodsName.isEmpty()) {
				// 尝试查找商品（可能是刚创建的）
				WmsGoodsSkuRespVO sku = findGoods(goodsName);
				if (sku != null) {
					state.setData("currentGoods", buildGoodsMap(sku, new HashMap<>()));
					state.setCurrentStep(STEP_GOODS_QUANTITY);
					return askGoodsQuantity(state, sku.getSkuName());
				}
				// 没找到商品，但用户刚创建过，直接进入数量输入步骤
				// 创建一个临时商品对象，包含基本字段
				Map<String, Object> tempGoods = new HashMap<>();
				tempGoods.put("name", goodsName);
				tempGoods.put("isNew", true);
				tempGoods.put("unit", "件"); // 默认单位，实际单位应该从表单获取
				tempGoods.put("spec", ""); // 规格可以为空
				tempGoods.put("skuId", null);
				tempGoods.put("goodsId", null);
				state.setData("currentGoods", tempGoods);
				state.setCurrentStep(STEP_GOODS_QUANTITY);
				return askGoodsQuantity(state, goodsName);
			}

			// 没有商品名称，询问用户输入
			state.setCurrentStep(STEP_GOODS);
			return response(state, "✅ 已返回对话\n\n请告诉我：**要采购什么商品？**");
		}

		if (isCancel(cleanMessage) || "返回".equals(cleanMessage) || "取消".equals(cleanMessage)) {
			state.removeData("tempGoodsName");
			state.removeData("tempGoodsInfoForCreate");
			state.removeData("tempOrderContext");
			state.setCurrentStep(STEP_GOODS);
			return response(state, "已取消商品创建，您可以重新输入采购商品信息。\n\n示例：\"采购3台苹果笔记本，单价8000元\"");
		}

		// 在创建页阶段，避免把无关输入误当成采购语句继续下单
		List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
		quickReplies.add(new ChatResponseVO.QuickReply("我已保存商品", "商品创建完成", "primary"));
		quickReplies.add(new ChatResponseVO.QuickReply("取消返回", "取消", "default"));
		return response(state,
				"当前正在等待商品创建结果。\n\n" +
				"请在商品页面保存后返回，或点击\"我已保存商品\"继续。")
				.withQuickReplies(quickReplies);
	}

	@SuppressWarnings("unchecked")
	private ChatResponseVO continueOrderWithNewGoods(ConversationState state, Long goodsId, Long skuId, String goodsName) {
		Map<String, Object> orderContext = (Map<String, Object>) state.getData("tempOrderContext");
		if (orderContext == null) {
			state.removeData("tempGoodsName");
			state.removeData("tempGoodsInfoForCreate");
			state.setCurrentStep(STEP_GOODS);
			return response(state, "✅ 商品创建完成，请继续告诉我要采购什么商品。");
		}

		Integer quantity = 1;
		Object quantityObj = orderContext.get("quantity");
		if (quantityObj != null) {
			try {
				quantity = Integer.parseInt(quantityObj.toString());
			} catch (Exception ignored) {
				quantity = 1;
			}
		}

		BigDecimal price = null;
		Object priceObj = orderContext.get("price");
		if (priceObj instanceof BigDecimal) {
			price = (BigDecimal) priceObj;
		} else if (priceObj != null) {
			try {
				price = new BigDecimal(priceObj.toString());
			} catch (Exception ignored) {
				price = null;
			}
		}

		String unit = orderContext.get("unit") == null ? "件" : orderContext.get("unit").toString();
		String supplier = orderContext.get("supplier") == null ? "" : orderContext.get("supplier").toString();

		Map<String, Object> item = new HashMap<>();
		item.put("skuId", skuId);
		item.put("goodsId", goodsId);
		item.put("name", nvl(goodsName, "新建商品"));
		item.put("spec", "");
		item.put("unit", unit);
		item.put("quantity", quantity);
		item.put("price", price);
		item.put("amount", price != null ? price.multiply(new BigDecimal(quantity)) : BigDecimal.ZERO);
		item.put("isNew", true);

		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
		if (items == null) {
			items = new ArrayList<>();
		}
		items.add(item);
		state.setData("items", items);

		if (!supplier.isEmpty()) {
			List<WmsSupplier> supplierList = findSupplierList(supplier);
			if (supplierList != null && !supplierList.isEmpty()) {
				WmsSupplier supplierEntity = supplierList.get(0);
				state.setData("supplierId", supplierEntity.getId());
				state.setData("supplierName", supplierEntity.getSupplierName());
				state.setData("isNewSupplier", false);
			} else {
				state.setData("supplierId", null);
				state.setData("supplierName", supplier);
				state.setData("isNewSupplier", true);
			}
		}

		state.removeData("tempGoodsName");
		state.removeData("tempGoodsInfoForCreate");
		state.removeData("tempOrderContext");
		return continueToConfirmOrAskSupplier(state);
	}

	private ChatResponseVO continueToConfirmOrAskSupplier(ConversationState state) {
		String supplierName = state.getData("supplierName", "");
		Long supplierId = toLong(state.getData("supplierId"));

		if ((supplierName == null || supplierName.trim().isEmpty()) && supplierId == null) {
			WmsSupplier defaultSupplier = pickDefaultSupplier(state);
			if (defaultSupplier != null) {
				state.setData("supplierId", defaultSupplier.getId());
				state.setData("supplierName", defaultSupplier.getSupplierName());
				state.setData("isNewSupplier", false);
			} else {
				state.setCurrentStep(STEP_SUPPLIER);
				return response(state, "请先补充供应商信息，再继续创建采购入库单。\n\n" + askForSupplier(state).getReply());
			}
		}

		state.setCurrentStep(STEP_CONFIRM_ORDER);
		return showOrderConfirm(state);
	}

	private void resolveSupplierFast(ConversationState state, String supplierInput) {
		List<WmsSupplier> supplierList = findSupplierList(supplierInput);
		if (supplierList != null && !supplierList.isEmpty()) {
			// 为减少链路步骤，匹配到多个供应商时默认取第一条（通常是最相关或最新）
			WmsSupplier supplierEntity = supplierList.get(0);
			state.setData("supplierId", supplierEntity.getId());
			state.setData("supplierName", supplierEntity.getSupplierName());
			state.setData("isNewSupplier", false);
			return;
		}

		state.setData("supplierId", null);
		state.setData("supplierName", supplierInput);
		state.setData("isNewSupplier", true);
	}

	@SuppressWarnings("unchecked")
	private WmsSupplier pickDefaultSupplier(ConversationState state) {
		// 优先从数据库查 is_default=1 的默认供应商
		List<WmsSupplier> defaultSuppliers = supplierService.list(
				Wrappers.<WmsSupplier>lambdaQuery()
						.eq(WmsSupplier::getIsDefault, 1)
						.eq(WmsSupplier::getStatus, "0")
						.last("LIMIT 1")
		);
		if (defaultSuppliers != null && !defaultSuppliers.isEmpty()) {
			return defaultSuppliers.get(0);
		}

		// 没有默认供应商，如果只有1个供应商则自动选中
		List<WmsSupplier> allSuppliers = supplierService.list(
				Wrappers.<WmsSupplier>lambdaQuery()
						.eq(WmsSupplier::getStatus, "0")
		);
		if (allSuppliers != null && allSuppliers.size() == 1) {
			return allSuppliers.get(0);
		}

		// 多个供应商且无默认，尝试从 start() 阶段缓存的列表取第一个
		List<WmsSupplier> availableSuppliers = (List<WmsSupplier>) state.getData("availableSuppliers");
		if (availableSuppliers != null && !availableSuppliers.isEmpty()) {
			return availableSuppliers.get(0);
		}

		return null;
	}

	/**
	 * 处理商品确认
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO handleGoodsConfirm(ConversationState state, String message) {
		// 清理语音识别可能添加的标点符号
		String cleanMessage = cleanPunctuation(message);

		if ("是这个商品".equals(cleanMessage) || (isYes(message) && !state.hasData("tempGoodsName"))) {
			// 用户确认是这个商品
			WmsGoodsSkuRespVO sku = (WmsGoodsSkuRespVO) state.getData("tempSelectedSku");
			Map<String, Object> goodsInfo = (Map<String, Object>) state.getData("tempGoodsInfo");

			if (sku != null) {
				state.setData("currentGoods", buildGoodsMap(sku, goodsInfo != null ? goodsInfo : new HashMap<>()));
				state.removeData("tempSelectedSku");
				state.removeData("tempGoodsInfo");
				return askGoodsQuantity(state, sku.getSkuName());
			}
		} else if ("创建新商品".equals(cleanMessage) || isCancel(message)) {
			// 用户要创建新商品
			String goodsName = "";
			if (state.hasData("tempSelectedSku")) {
				WmsGoodsSkuRespVO sku = (WmsGoodsSkuRespVO) state.getData("tempSelectedSku");
				goodsName = sku != null ? sku.getSkuName() : "";
			}
			state.removeData("tempSelectedSku");
			state.removeData("tempGoodsInfo");

			state.setData("tempGoodsName", goodsName);
			state.setCurrentStep(STEP_GOODS_CREATE_PAGE);

			// 构建导航操作
			Map<String, Object> arguments = new HashMap<>();
			arguments.put("goodsName", goodsName);
			arguments.put("fromChat", "true");
			arguments.put("conversationId", state.getConversationId());

			ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
			action.setName("create_goods");
			action.setDescription("前往商品创建页面");
			action.setArguments(arguments);

			return ChatResponseVO.builder()
					.conversationId(state.getConversationId())
					.operationType(getOperationType())
					.reply("好的，请前往**商品创建页面**创建新商品。\n\n" +
							"- 点击「执行操作」前往创建页面\n" +
							"- 创建完成后将自动返回继续对话")
					.suggestedAction(action)
					.collectedData(state.getCollectedData())
					.needMoreInfo(true)
					.build();
		}

		// 其他输入，重新搜索
		state.removeData("tempSelectedSku");
		state.removeData("tempGoodsInfo");
		state.setCurrentStep(STEP_GOODS);
		return response(state, "请重新告诉我：**要采购什么商品？**");
	}

	private ChatResponseVO handleGoodsNew(ConversationState state, String message) {
		if (!isYes(message)) {
			state.removeData("tempGoodsName");
			state.setCurrentStep(STEP_GOODS);
			return responseWithSupplierList(state, "请重新选择**供应商**（输入序号或供应商名称）：");
		}

		// 用户确认新建商品，显示确认并引导输入商品信息
		state.setCurrentStep(STEP_GOODS_NEW_CONFIRM);
		return response(state, "✅ 将创建新商品：**" + state.getData("tempGoodsName") + "**\n\n" +
				"请提供商品的基本信息：\n\n" +
				"📝 **请一次性告诉我**（可以按任意格式）：\n\n" +
				"**必填：**\n" +
				"- 规格（如：500ml/瓶）\n" +
				"- 单位（如：瓶、箱）\n\n" +
				"**选填：**\n" +
				"- 条形码\n" +
				"- 采购价\n" +
				"- 销售价\n\n" +
				"📎 示例：500ml/瓶 瓶 6901234567890 45 60");
	}

	private ChatResponseVO handleGoodsNewConfirm(ConversationState state, String message) {
		Map<String, String> info = parseNewGoodsInfo(message);

		String skuName = (String) state.getData("tempGoodsName");
		String spec = info.get("spec");
		String unit = info.get("unit");
		String barcode = info.get("barcode");
		String purchasePrice = info.get("purchasePrice");
		String salePrice = info.get("salePrice");

		// 验证必填字段
		if (skuName == null || skuName.isEmpty()) {
			skuName = (String) state.getData("tempGoodsName");
		}
		if (spec == null || spec.isEmpty()) {
			return response(state, "请告诉我商品的**规格**是什么？（如：500ml/瓶）");
		}
		if (unit == null || unit.isEmpty()) {
			return response(state, "请告诉我商品的**单位**是什么？（如：瓶、箱、个）");
		}

		// 保存商品信息
		Map<String, Object> goods = new HashMap<>();
		goods.put("name", skuName);
		goods.put("spec", spec);
		goods.put("unit", unit);
		goods.put("barcode", barcode != null ? barcode : "");
		goods.put("purchasePrice", purchasePrice != null ? new BigDecimal(purchasePrice) : null);
		goods.put("salePrice", salePrice != null ? new BigDecimal(salePrice) : null);
		goods.put("isNew", true);
		state.setData("currentGoods", goods);
		state.setCurrentStep(STEP_GOODS_QUANTITY);

		return askGoodsQuantity(state, skuName);
	}

	private ChatResponseVO handleGoodsQuantity(ConversationState state, String message) {
		Integer quantity = parseQuantity(message);
		if (quantity == null || quantity <= 0) {
			@SuppressWarnings("unchecked")
			Map<String, Object> goods = (Map<String, Object>) state.getData("currentGoods");
			String name = goods != null ? (String) goods.get("name") : "商品";
			return response(state, "请输入**数量**（如：100、50）？");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> goods = (Map<String, Object>) state.getData("currentGoods");
		goods.put("quantity", quantity);
		state.setData("currentGoods", goods);
		state.setCurrentStep(STEP_GOODS_PRICE);

		String name = goods != null ? (String) goods.get("name") : "商品";
		return response(state, "📦 " + name + "\n\n请输入**采购单价**（元）？\n\n💡 后续可输入 \"数量改XX\" 或 \"单价改XX\" 修改");
	}

	private ChatResponseVO handleGoodsPrice(ConversationState state, String message) {
		BigDecimal price = parsePrice(message);
		if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
			return response(state, "请输入有效的**单价**（如：50、45.5）？");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> goods = (Map<String, Object>) state.getData("currentGoods");
		goods.put("price", price);

		// 计算金额
		BigDecimal quantity = new BigDecimal(goods.get("quantity").toString());
		goods.put("amount", quantity.multiply(price));

		// 添加到商品列表
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
		items.add(goods);
		state.setData("items", items);
		state.removeData("currentGoods");

		state.setCurrentStep(STEP_MORE_GOODS);
		return askMoreGoods(state);
	}

	private ChatResponseVO askMoreGoods(ConversationState state) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");

		StringBuilder sb = new StringBuilder();
		sb.append("✅ 商品已添加！\n\n");
		sb.append("📦 **已添加的商品：**\n");
		for (int i = 0; i < items.size(); i++) {
			Map<String, Object> item = items.get(i);
			sb.append("\n").append(i + 1).append(". ").append(item.get("name"));

			// 规格可选
			Object spec = item.get("spec");
			if (spec != null && !spec.toString().isEmpty()) {
				sb.append(" (").append(spec).append(")");
			}

			// 单位可选，默认为"件"
			Object unit = item.get("unit");
			if (unit == null) {
				unit = "件";
			}

			sb.append("\n   数量：").append(item.get("quantity")).append(unit)
					.append(" × ¥").append(item.get("price"))
					.append(" = ¥").append(item.get("amount"));
		}

		sb.append("\n\n还要继续添加商品吗？");

		// 构建快捷回复选项
		List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
		quickReplies.add(new ChatResponseVO.QuickReply("继续添加", "继续添加", "default"));
		quickReplies.add(new ChatResponseVO.QuickReply("下一步", "下一步", "primary"));

		return response(state, sb.toString()).withQuickReplies(quickReplies);
	}

	private ChatResponseVO handleMoreGoods(ConversationState state, String message) {
		// 去掉语音识别可能添加的标点符号
		String cleanMessage = cleanPunctuation(message);

		// 处理快捷回复
		if ("下一步".equals(cleanMessage) || "完成订单".equals(cleanMessage) || isConfirm(cleanMessage) || "完成".equals(cleanMessage)) {
			// 检查是否已选择供应商
			if (!state.hasData("supplierName")) {
				state.setCurrentStep(STEP_SUPPLIER);
				return askForSupplier(state);
			}
			state.setCurrentStep(STEP_ORDER_INFO);
			return askOrderInfo(state);
		}

		if ("继续添加".equals(cleanMessage)) {
			state.setCurrentStep(STEP_GOODS);
			return response(state, "请告诉我：**要采购什么商品？**");
		}

		// 继续添加商品 - 将输入作为商品名称处理
		state.setCurrentStep(STEP_GOODS);
		return handleGoods(state, message);
	}

	// ==================== 订单其他信息 ====================

	private ChatResponseVO askOrderInfo(ConversationState state) {
		state.setCurrentStep(STEP_ORDER_INFO);
		// 构建快捷回复选项
		List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
		quickReplies.add(new ChatResponseVO.QuickReply("跳过", "跳过", "default"));
		quickReplies.add(new ChatResponseVO.QuickReply("完成订单", "完成订单", "primary"));

		return response(state, "📋 **订单其他信息**\n\n" +
				"请告诉我以下信息（选填，直接输入或跳过）：\n\n" +
				"- 交货日期（如：明天、下周三、2024-03-15）\n" +
				"- 经办人姓名\n" +
				"- 备注信息").withQuickReplies(quickReplies);
	}

	private ChatResponseVO handleOrderInfo(ConversationState state, String message) {
		// 如果消息为空或者是首次进入，显示提示
		if (message == null || message.trim().isEmpty()) {
			return askOrderInfo(state);
		}

		// 先检查快捷回复值
		if ("完成订单".equals(message) || "完成".equals(message) || "跳过".equals(message)) {
			state.setCurrentStep(STEP_CONFIRM_ORDER);
			return showOrderConfirm(state);
		}

		// 检查是否为传统确认输入
		if (isConfirm(message)) {
			state.setCurrentStep(STEP_CONFIRM_ORDER);
			return showOrderConfirm(state);
		}

		// 尝试解析信息
		String deliveryDate = parseDeliveryDate(message);
		if (deliveryDate != null) {
			state.setData("deliveryDate", deliveryDate);
			return response(state, "✅ 交货日期已设置为：" + deliveryDate + "\n\n" +
					"还需要填写其他信息吗？\n" +
					"- 经办人\n" +
					"- 备注\n" +
					"- 或点击「完成订单」查看订单确认");
		}

		// 判断是否是经办人
		if (message.matches("^[\\u4e00-\\u9fa5a-zA-Z]{2,10}$")) {
			state.setData("handler", message);
			return response(state, "✅ 经办人已设置为：" + message + "\n\n" +
					"还需要填写其他信息吗？\n" +
					"- 交货日期\n" +
					"- 备注\n" +
					"- 或点击「完成订单」查看订单确认");
		}

		// 作为备注处理
		String remark = state.getData("remark", "");
		state.setData("remark", remark.isEmpty() ? message : remark + "; " + message);
		return response(state, "✅ 已记录备注信息\n\n" +
				"还需要填写其他信息吗？\n" +
				"- 交货日期\n" +
				"- 经办人\n" +
				"- 或点击「完成订单」查看订单确认");
	}

	// ==================== 订单确认 ====================

	private ChatResponseVO showOrderConfirm(ConversationState state) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");

		if (items.isEmpty()) {
			state.setCurrentStep(STEP_GOODS);
			return response(state, "⚠️ 还没有添加任何商品！\n\n请告诉我：**要采购什么商品？**");
		}

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (Map<String, Object> item : items) {
			totalAmount = totalAmount.add(new BigDecimal(item.get("amount").toString()));
		}

		StringBuilder sb = new StringBuilder();
		sb.append("📋 **采购入库确认**\n\n");
		sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

		// 供应商信息
		sb.append("📁 供应商：").append(state.getData("supplierName"));
		if (Boolean.TRUE.equals(state.getData("isNewSupplier"))) {
			sb.append("（新供应商*）");
		}
		sb.append("\n");

		// 订单信息
		String orderDate = LocalDate.now().format(DATE_FORMATTER);
		sb.append("📅 订单日期：").append(orderDate).append("\n");
		String warehouseName = state.getData("warehouseName", "默认仓库");
		sb.append("🏠 仓库：").append(warehouseName).append("\n");

		String deliveryDate = (String) state.getData("deliveryDate");
		if (deliveryDate != null) {
			sb.append("📦 交货日期：").append(deliveryDate).append("\n");
		}

		String handler = (String) state.getData("handler");
		if (handler != null) {
			sb.append("👤 经办人：").append(handler).append("\n");
		}

		sb.append("💰 总金额：¥").append(totalAmount).append("\n");
		sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

		// 商品明细
		sb.append("\n📦 **商品明细：**\n");
		for (int i = 0; i < items.size(); i++) {
			Map<String, Object> item = items.get(i);
			sb.append("\n").append(i + 1).append(". **").append(item.get("name")).append("**");

			// 规格可选
			Object spec = item.get("spec");
			if (spec != null && !spec.toString().isEmpty()) {
				sb.append(" (").append(spec).append(")");
			}

			// 数量
			sb.append("\n   数量：").append(item.get("quantity"));

			// 单位可选
			Object unit = item.get("unit");
			if (unit != null) {
				sb.append(unit);
			} else {
				sb.append("件");
			}

			// 单价
			sb.append("\n   单价：¥").append(item.get("price"));

			// 金额
			sb.append("\n   金额：¥").append(item.get("amount"));

			if (Boolean.TRUE.equals(item.get("isNew"))) {
				sb.append("\n   *新商品");
			}
		}

		String remark = (String) state.getData("remark");
		if (remark != null && !remark.isEmpty()) {
			sb.append("\n\n📝 备注：").append(remark);
		}

		sb.append("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
		if (isAutoApprovePurchaseIn()) {
			sb.append("当前单据为自动审核模式：仅允许修改供应商等非关键字段，数量/单价不可修改。\n\n");
		} else {
			sb.append("当前单据为人工审核模式：可在确认前修改数量/单价/供应商。\n\n");
		}
		sb.append("请确认订单信息：");
		if (Boolean.TRUE.equals(state.getData("isNewSupplier")) ||
				items.stream().anyMatch(i -> Boolean.TRUE.equals(i.get("isNew")))) {
			sb.append("\n\n* 标记为新建的项目将一并创建");
		}

		// 构建快捷回复选项 - 只有确认按钮，用户可直接说"修改XX"来修改
		List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
		quickReplies.add(new ChatResponseVO.QuickReply("确认创建", "确认", "primary"));
		quickReplies.add(new ChatResponseVO.QuickReply("修改订单", "跳转修改页面", "default"));

		return response(state, sb.toString()).withQuickReplies(quickReplies);
	}

	private ChatResponseVO handleConfirmOrder(ConversationState state, String message) {
		log.info("handleConfirmOrder 被调用, message='{}'", message);

		// 处理跳转修改页面
		if ("跳转修改页面".equals(message)) {
			return navigateToModifyPage(state);
		}

		// 处理确认操作
		if ("确认".equals(message) || "确认创建".equals(message) || "确认保存".equals(message) || isConfirm(message)) {
			log.info("开始执行 confirm 方法");
			return confirm(state);
		}

		// 处理修改指令 - 支持 "修改数量为100", "把单价改成50", "供应商换成XX" 等格式
		if (message.contains("修改") || message.contains("改成") || message.contains("换成") || message.contains("改为")) {
			return handleModifyOrder(state, message);
		}

		// 其他输入，重新显示确认页面
		log.info("不匹配确认条件，重新显示确认页面");
		return showOrderConfirm(state);
	}

	/**
	 * 处理订单修改指令
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO handleModifyOrder(ConversationState state, String message) {
		log.info("处理修改指令: {}", message);

		if (isAutoApprovePurchaseIn() && isQuantityOrPriceModifyIntent(message)) {
			return response(state,
					"当前采购入库为自动审核模式，数量和单价属于关键字段，创建前不允许在会话中修改。\n\n" +
					"您仍可修改供应商等非关键信息，例如：\"供应商换成XX公司\"。\n\n" +
					"如需修改数量/单价，请切换为人工审核策略后再操作。")
					.withQuickReplies(buildConfirmQuickReplies());
		}

		// 解析并更新数量： "数量改成100" / "修改数量为50" / "把数量增加到200"
		if (message.matches(".*数[量个].*(\\d+).*") || message.matches(".*改为.*") || message.contains("增加")) {
			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
			java.util.regex.Matcher matcher = pattern.matcher(message);
			if (matcher.find()) {
				String newQuantityStr = matcher.group();
				try {
					int newQuantity = Integer.parseInt(newQuantityStr);
					List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
					if (!items.isEmpty()) {
						Map<String, Object> lastItem = items.get(items.size() - 1);
						lastItem.put("quantity", newQuantity);
						// 重新计算金额
						BigDecimal price = new BigDecimal(lastItem.get("price").toString());
						lastItem.put("amount", price.multiply(new BigDecimal(newQuantity)));
					}
					return showOrderConfirm(state);
				} catch (NumberFormatException e) {
					log.warn("解析数量失败", e);
				}
			}
		}

		// 解析并更新单价： "单价改成50" / "修改单价为80"
		if (message.contains("单价") && (message.contains("改成") || message.contains("改为") || message.contains("为"))) {
			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)");
			java.util.regex.Matcher matcher = pattern.matcher(message);
			if (matcher.find()) {
				String newPriceStr = matcher.group();
				try {
					BigDecimal newPrice = new BigDecimal(newPriceStr);
					List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
					if (!items.isEmpty()) {
						Map<String, Object> lastItem = items.get(items.size() - 1);
						lastItem.put("price", newPrice);
						// 重新计算金额
						BigDecimal quantity = new BigDecimal(lastItem.get("quantity").toString());
						lastItem.put("amount", quantity.multiply(newPrice));
					}
					return showOrderConfirm(state);
				} catch (NumberFormatException e) {
					log.warn("解析单价失败", e);
				}
			}
		}

		// 解析并更新供应商： "供应商换成XX公司" / "修改供应商为YY"
		if (message.contains("供应商") && (message.contains("换成") || message.contains("改为") || message.contains("为"))) {
			// 提取供应商名称
			String newSupplierName = extractSupplierName(message);
			if (newSupplierName != null && !newSupplierName.isEmpty()) {
				// 查找供应商
				WmsSupplier supplier = findSupplier(newSupplierName);
				if (supplier != null) {
					state.setData("supplierId", supplier.getId());
					state.setData("supplierName", supplier.getSupplierName());
					return response(state, "✅ 供应商已更新为：" + newSupplierName + "\n\n" + getCurrentOrderSummary(state) + "\n\n是否确认创建？").withQuickReplies(buildConfirmQuickReplies());
				} else {
					// 未找到供应商，引导创建
					state.setData("tempSupplierName", newSupplierName);
					state.setData("isNewSupplier", true);
					return response(state, "未找到供应商「" + newSupplierName + "」，是否创建新供应商？（回复「创建」/「取消」）").withYesNoReplies();
				}
			}
		}

		// 无法识别的修改指令
		return response(state, "抱歉，我没有理解您的修改意图。\n\n您可以说：\n" +
				"- \"数量改成100\"\n" +
				"- \"单价改成50\"\n" +
				"- \"供应商换成XX公司\"\n\n" +
				getCurrentOrderSummary(state) + "\n\n是否确认创建？").withQuickReplies(buildConfirmQuickReplies());
	}

	/**
	 * 提取供应商名称
	 */
	private String extractSupplierName(String message) {
		// 匹配 "供应商换成XXX" 或 "修改供应商为XXX" 或 "供应商改为XXX"
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("供应商[换为改]*([^，。！？\\n]+)");
		java.util.regex.Matcher matcher = pattern.matcher(message);
		if (matcher.find()) {
			return matcher.group(1).trim();
		}
		return null;
	}

	/**
	 * 获取当前订单摘要
	 */
	@SuppressWarnings("unchecked")
	private String getCurrentOrderSummary(ConversationState state) {
		StringBuilder sb = new StringBuilder();
		sb.append("**当前订单**：\n");
		sb.append("供应商：").append(state.getData("supplierName")).append("\n");

		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
		if (!items.isEmpty()) {
			Map<String, Object> item = items.get(0);
			sb.append("商品：").append(item.get("name"));
			sb.append("（").append(item.get("unit")).append("）");
			sb.append(" x ").append(item.get("quantity"));
			sb.append(" @ ¥").append(item.get("price"));
		}
		return sb.toString();
	}

	/**
	 * 构建确认快捷回复
	 */
	private List<ChatResponseVO.QuickReply> buildConfirmQuickReplies() {
		List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
		quickReplies.add(new ChatResponseVO.QuickReply("确认创建", "确认", "primary"));
		return quickReplies;
	}

	private boolean isAutoApprovePurchaseIn() {
		try {
			return !approveFlowService.needApprove(WmsApproveConstants.BILL_TYPE_PURCHASE_IN);
		} catch (Exception e) {
			log.warn("检测采购入库审核模式失败，默认按人工审核处理", e);
			return false;
		}
	}

	private boolean isQuantityOrPriceModifyIntent(String message) {
		if (message == null || message.trim().isEmpty()) {
			return false;
		}
		return message.contains("数量") || message.contains("数目")
				|| message.contains("单价") || message.contains("价格")
				|| message.contains("金额") || message.contains("改价");
	}

	@Override
	public ChatResponseVO confirm(ConversationState state) {
		log.info("=== confirm 方法开始执行 ===");
		try {
			// 1. 处理供应商（如果是新供应商则创建）
			log.info("步骤1: 处理供应商");
			Long supplierId = handleSupplierCreation(state);
			log.info("供应商ID: {}", supplierId);

			// 2. 构建入库单保存请求
			com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInSaveReqVO orderReq =
				new com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInSaveReqVO();

			orderReq.setSupplierId(supplierId);
			orderReq.setWarehouseId(getDefaultWarehouseId(state));
			orderReq.setInDate(LocalDate.now());
			orderReq.setSourceType(1);
			orderReq.setHandler((String) state.getData("handler"));
			orderReq.setRemark((String) state.getData("remark"));

			// 3. 构建订单明细
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");

			List<com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemSaveReqVO> itemReqList = new ArrayList<>();

			for (Map<String, Object> item : items) {
				com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemSaveReqVO itemReq =
					new com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemSaveReqVO();

				// 如果是新商品（skuId为空），使用商品名称，让系统自动创建
				Object skuIdObj = item.get("skuId");
				if (skuIdObj != null && skuIdObj instanceof Number) {
					itemReq.setSkuId(((Number) skuIdObj).longValue());
				}

				// 数量和单价
				Object quantityObj = item.get("quantity");
				if (quantityObj instanceof Number) {
					itemReq.setQuantity(new BigDecimal(quantityObj.toString()));
				} else {
					itemReq.setQuantity(BigDecimal.ONE);
				}

				Object priceObj = item.get("price");
				if (priceObj instanceof Number) {
					itemReq.setPrice(new BigDecimal(priceObj.toString()));
				}

				itemReqList.add(itemReq);
			}

			orderReq.setItems(itemReqList);

			// 4. 调用service保存入库单
			Long orderId = purchaseOrderService.create(orderReq);
			log.info("采购入库单创建成功: orderId={}", orderId);

			// 5. 获取创建的入库单信息
			com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO createdOrder = purchaseOrderService.get(orderId);
			String orderNo = createdOrder != null ? createdOrder.getInNo() : "RK" + System.currentTimeMillis();

			// 6. 构建成功响应（传递 orderId）
			return buildSuccessResponse(state, orderNo, orderId, items);

		} catch (Exception e) {
			log.error("创建采购入库单失败", e);
			state.setStatus(ConversationState.SessionStatus.CANCELLED);
			return response(state, "❌ **创建采购入库单失败**\n\n" + e.getMessage() + "\n\n请稍后重试或联系管理员。").isCompleted(true);
		}
	}

	private Long getDefaultWarehouseId(ConversationState state) {
		Object warehouseIdObj = state.getData("warehouseId");
		if (warehouseIdObj instanceof Number) {
			return ((Number) warehouseIdObj).longValue();
		}
		List<WmsWarehouse> warehouses = warehouseService.list(
				Wrappers.<WmsWarehouse>lambdaQuery()
						.eq(WmsWarehouse::getIsDefault, 1)
						.orderByDesc(WmsWarehouse::getCreateTime)
						.last("LIMIT 1")
		);
		if (warehouses == null || warehouses.isEmpty()) {
			warehouses = warehouseService.list(
					Wrappers.<WmsWarehouse>lambdaQuery().orderByDesc(WmsWarehouse::getCreateTime).last("LIMIT 1")
			);
		}
		if (warehouses != null && !warehouses.isEmpty()) {
			WmsWarehouse warehouse = warehouses.get(0);
			state.setData("warehouseId", warehouse.getId());
			state.setData("warehouseName", warehouse.getWarehouseName());
			return warehouse.getId();
		}
		throw new RuntimeException("未找到可用仓库，请先创建仓库");
	}

	/**
	 * 处理供应商创建（如果是新供应商）
	 */
	private Long handleSupplierCreation(ConversationState state) {
		Object supplierIdObj = state.getData("supplierId");

		// 如果已有供应商ID，直接返回
		if (supplierIdObj != null && supplierIdObj instanceof Number) {
			Long id = ((Number) supplierIdObj).longValue();
			if (id > 0) {
				return id;
			}
		}

		// 如果是新供应商，创建供应商
		if (Boolean.TRUE.equals(state.getData("isNewSupplier"))) {
			String supplierName = (String) state.getData("supplierName");
			if (supplierName != null && !supplierName.isEmpty()) {
				com.ruoyi.wms.domain.vo.supplier.WmsSupplierSaveReqVO supplierReq =
					new com.ruoyi.wms.domain.vo.supplier.WmsSupplierSaveReqVO();

				// 生成供应商编码
				String supplierCode = "SY" + System.currentTimeMillis();
				supplierReq.setSupplierCode(supplierCode);
				supplierReq.setSupplierName(supplierName);
				supplierReq.setStatus("0"); // 正常状态
				supplierReq.setRemark("AI助手自动创建");

				Long newSupplierId = supplierService.create(supplierReq);
				log.info("自动创建供应商成功: supplierId={}, supplierName={}", newSupplierId, supplierName);
				return newSupplierId;
			}
		}

		throw new RuntimeException("供应商信息不完整，无法创建订单");
	}

	/**
	 * 构建成功响应
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO buildSuccessResponse(ConversationState state, String orderNo, Long orderId, List<Map<String, Object>> items) {
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (Map<String, Object> item : items) {
			Object amountObj = item.get("amount");
			if (amountObj instanceof Number) {
				totalAmount = totalAmount.add(new BigDecimal(amountObj.toString()));
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("✅ **采购入库单创建成功！**\n\n");
		sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
		sb.append("📄 入库单号：").append(orderNo).append("\n");
		sb.append("📁 供应商：").append(state.getData("supplierName")).append("\n");
		String warehouseName = state.getData("warehouseName", "默认仓库");
		sb.append("🏠 仓库：").append(warehouseName).append("\n");
		sb.append("📅 日期：").append(LocalDate.now().format(DATE_FORMATTER)).append("\n");

		String deliveryDate = (String) state.getData("deliveryDate");
		if (deliveryDate != null) {
			sb.append("📦 交货日期：").append(deliveryDate).append("\n");
		}

		String handler = (String) state.getData("handler");
		if (handler != null) {
			sb.append("👤 经办人：").append(handler).append("\n");
		}

		sb.append("💰 总金额：¥").append(totalAmount).append("\n");
		sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

		// 商品明细
		sb.append("\n📦 **商品明细：**\n");
		for (int i = 0; i < items.size(); i++) {
			Map<String, Object> item = items.get(i);
			sb.append("\n").append(i + 1).append(". **").append(item.get("name")).append("**");

			// 规格可选
			Object spec = item.get("spec");
			if (spec != null && !spec.toString().isEmpty()) {
				sb.append(" (").append(spec).append(")");
			}

			// 单位可选
			Object unit = item.get("unit");
			if (unit != null) {
				sb.append("\n   数量：").append(item.get("quantity")).append(unit);
			} else {
				sb.append("\n   数量：").append(item.get("quantity")).append("件");
			}

			sb.append("\n   单价：¥").append(item.get("price"));

			Object amountObj = item.get("amount");
			if (amountObj != null) {
				sb.append("\n   金额：¥").append(amountObj);
			}

			if (Boolean.TRUE.equals(item.get("isNew"))) {
				sb.append("\n   *新商品");
			}
		}

		String remark = (String) state.getData("remark");
		if (remark != null && !remark.isEmpty()) {
			sb.append("\n\n📝 备注：").append(remark);
		}

		sb.append("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

		// 显示新建的项目
		List<String> newItems = new ArrayList<>();
		if (Boolean.TRUE.equals(state.getData("isNewSupplier"))) {
			newItems.add("供应商：" + state.getData("supplierName"));
		}
		items.stream()
				.filter(i -> Boolean.TRUE.equals(i.get("isNew")))
				.forEach(i -> newItems.add("商品：" + i.get("name")));

		if (!newItems.isEmpty()) {
			sb.append("📝 同时创建了以下新数据：\n");
			for (String item : newItems) {
				sb.append("  • ").append(item).append("\n");
			}
			sb.append("\n");
		}

		state.setStatus(ConversationState.SessionStatus.COMPLETED);

		// 添加跳转到订单编辑页面的操作按钮
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("id", orderId.toString());

		ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
		action.setName("edit_purchase_in");
		action.setDescription("查看/编辑入库单");
		action.setArguments(arguments);

		// 添加快捷回复选项：继续创建订单
		List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
		quickReplies.add(new ChatResponseVO.QuickReply("创建下一条入库单", "创建采购入库", "primary"));

		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(sb.toString())
				.suggestedAction(action)
				.quickReplies(quickReplies)
				.collectedData(state.getCollectedData())
				.isCompleted(true)
				.build();
	}

	@Override
	public ChatResponseVO cancel(ConversationState state) {
		state.setStatus(ConversationState.SessionStatus.CANCELLED);
		return response(state, "❌ 已取消创建采购入库单。").isCompleted(true);
	}

	/**
	 * 处理完整订单（智能解析后直接生成预览）
	 * 支持语音/文本一次性输入完整订单信息
	 */
	@SuppressWarnings("unchecked")
	public ChatResponseVO handleCompleteOrder(ConversationState state, ParsedOrderResult parsedResult) {
		log.info("处理完整采购入库: {}", parsedResult.getOriginalMessage());

		// 初始化商品列表
		state.setData("items", new ArrayList<Map<String, Object>>());
		state.setCurrentStep(STEP_CONFIRM_ORDER);

		// 处理供应商信息
		if (parsedResult.hasSupplier()) {
			String supplierName = parsedResult.getSupplier();
			WmsSupplier supplier = findSupplier(supplierName);
			if (supplier != null) {
				state.setData("supplierId", supplier.getId());
				state.setData("supplierName", supplier.getSupplierName());
				state.setData("isNewSupplier", false);
			} else {
				state.setData("supplierId", null);
				state.setData("supplierName", supplierName);
				state.setData("isNewSupplier", true);
			}
		}

		// 设置默认仓库
		List<WmsWarehouse> warehouses = warehouseService.list(
				Wrappers.<WmsWarehouse>lambdaQuery()
						.orderByDesc(WmsWarehouse::getIsDefault)
						.orderByDesc(WmsWarehouse::getCreateTime)
		);
		if (warehouses != null && !warehouses.isEmpty()) {
			state.setData("availableWarehouses", warehouses);
			state.setData("warehouseId", warehouses.get(0).getId());
			state.setData("warehouseName", warehouses.get(0).getWarehouseName());
		}

		// 处理商品信息
		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
		for (ParsedOrderResult.GoodsItem goodsItem : parsedResult.getGoods()) {
			Map<String, Object> item = new HashMap<>();

			// 尝试匹配商品
			WmsGoodsSkuRespVO sku = findGoods(goodsItem.getName());
			if (sku != null) {
				item.put("skuId", sku.getId());
				item.put("goodsId", sku.getGoodsId());
				item.put("name", sku.getSkuName());
				item.put("spec", sku.getSpecInfo() != null ? sku.getSpecInfo() : goodsItem.getSpec());
				item.put("isNew", false);
			} else {
				item.put("skuId", null);
				item.put("goodsId", null);
				item.put("name", goodsItem.getName());
				item.put("spec", goodsItem.getSpec());
				item.put("isNew", true);
			}

			// 设置数量和价格
			item.put("quantity", goodsItem.getQuantity());
			item.put("price", goodsItem.getPrice());
			item.put("unit", goodsItem.getUnit());

			// 计算金额
			if (goodsItem.getQuantity() != null && goodsItem.getPrice() != null) {
				item.put("amount", goodsItem.getPrice().multiply(new BigDecimal(goodsItem.getQuantity())));
			}

			items.add(item);
		}

		return continueToConfirmOrAskSupplier(state);
	}

	@Override
	public String getStepPrompt(ConversationState state) {
		return null;
	}

	// ==================== 辅助方法 ====================

	private ChatResponseVO response(ConversationState state, String reply) {
		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(reply)
				.needMoreInfo(true)
				.collectedData(state.getCollectedData())
				.build();
	}

	private ChatResponseVO response(ConversationState state, String reply, boolean needMoreInfo) {
		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(reply)
				.needMoreInfo(needMoreInfo)
				.collectedData(state.getCollectedData())
				.build();
	}

	private ChatResponseVO responseWithWarehouseList(ConversationState state, String message) {
		StringBuilder sb = new StringBuilder(message);
		@SuppressWarnings("unchecked")
		List<WmsWarehouse> warehouses = (List<WmsWarehouse>) state.getData("availableWarehouses");
		if (warehouses != null && !warehouses.isEmpty()) {
			sb.append("\n\n可用仓库：\n");
			for (int i = 0; i < warehouses.size(); i++) {
				sb.append(i + 1).append(". ").append(warehouses.get(i).getWarehouseName()).append("\n");
			}
			sb.append(warehouses.size() + 1).append(". 新建仓库\n");
		}
		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(sb.toString())
				.needMoreInfo(true)
				.pendingFields(Arrays.asList("warehouse"))
				.collectedData(state.getCollectedData())
				.build();
	}

	private ChatResponseVO responseWithSupplierList(ConversationState state, String message) {
		StringBuilder sb = new StringBuilder(message);
		@SuppressWarnings("unchecked")
		List<WmsSupplier> suppliers = (List<WmsSupplier>) state.getData("availableSuppliers");
		if (suppliers != null && !suppliers.isEmpty()) {
			sb.append("\n\n常用供应商（输入关键字即可）：\n");
			for (int i = 0; i < suppliers.size(); i++) {
				sb.append(i + 1).append(". ").append(suppliers.get(i).getSupplierName()).append("\n");
			}
			sb.append(suppliers.size() + 1).append(". 新建供应商\n");
		}
		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(sb.toString())
				.needMoreInfo(true)
				.pendingFields(Arrays.asList("supplier"))
				.collectedData(state.getCollectedData())
				.build();
	}

	private ChatResponseVO askGoodsQuantity(ConversationState state, String goodsName) {
		state.setCurrentStep(STEP_GOODS_QUANTITY);

		// 检查商品是否有采购价格
		@SuppressWarnings("unchecked")
		Map<String, Object> currentGoods = (Map<String, Object>) state.getData("currentGoods");
		BigDecimal purchasePrice = null;
		if (currentGoods != null && currentGoods.get("purchasePrice") != null) {
			Object priceObj = currentGoods.get("purchasePrice");
			if (priceObj instanceof BigDecimal) {
				purchasePrice = (BigDecimal) priceObj;
			} else if (priceObj != null) {
				purchasePrice = new BigDecimal(priceObj.toString());
			}
		}

		StringBuilder reply = new StringBuilder();
		reply.append("📦 ").append(goodsName).append("\n\n");
		if (purchasePrice != null) {
			reply.append("💰 参考采购价：¥").append(purchasePrice).append("\n\n");
		}
		reply.append("请输入**数量**（如：100、50）？");

		return response(state, reply.toString());
	}

	// ==================== 数据查找 ====================

	private WmsSupplier findSupplier(String name) {
		List<WmsSupplier> suppliers = supplierService.list(
				Wrappers.<WmsSupplier>lambdaQuery()
						.like(WmsSupplier::getSupplierName, name)
						.orderByDesc(WmsSupplier::getCreateTime)
						.last("LIMIT 1")
		);
		return suppliers.isEmpty() ? null : suppliers.get(0);
	}

	/**
	 * 查找供应商列表（返回最新3条）
	 */
	private List<WmsSupplier> findSupplierList(String name) {
		try {
			List<WmsSupplier> suppliers = supplierService.list(
					Wrappers.<WmsSupplier>lambdaQuery()
							.like(WmsSupplier::getSupplierName, name)
							.orderByDesc(WmsSupplier::getCreateTime)
							.last("LIMIT 3")
			);
			return suppliers != null ? suppliers : new ArrayList<>();
		} catch (Exception e) {
			log.warn("查找供应商列表失败: {}", name, e);
			return new ArrayList<>();
		}
	}

	private WmsWarehouse findWarehouseInState(ConversationState state, String input) {
		@SuppressWarnings("unchecked")
		List<WmsWarehouse> warehouses = (List<WmsWarehouse>) state.getData("availableWarehouses");
		if (warehouses == null || warehouses.isEmpty()) {
			return null;
		}

		// 检查是否选择"新建"
		if ("新建".equals(input) || input.contains("新建")) {
			return null; // 返回null会触发新建仓库流程
		}

		// 尝试按序号匹配
		if (input.matches("\\d+")) {
			int index = Integer.parseInt(input) - 1;
			if (index >= 0 && index < warehouses.size()) {
				return warehouses.get(index);
			}
			// 检查是否选择"新建仓库"（序号是数量+1）
			if (index == warehouses.size()) {
				return null;
			}
		}

		// 按名称模糊匹配
		for (WmsWarehouse w : warehouses) {
			if (w.getWarehouseName().contains(input) || input.contains(w.getWarehouseName())) {
				return w;
			}
		}
		return null;
	}

	private WmsGoodsSkuRespVO findGoods(String name) {
		try {
			List<WmsGoodsSkuRespVO> skus = goodsSkuService.searchByName(name, 5);
			return skus.isEmpty() ? null : skus.get(0);
		} catch (Exception e) {
			log.warn("查找商品失败: {}", name, e);
			return null;
		}
	}

	/**
	 * 查找商品列表（返回最新3条）
	 */
	private List<WmsGoodsSkuRespVO> findGoodsList(String name) {
		try {
			return goodsSkuService.searchByName(name, 3);
		} catch (Exception e) {
			log.warn("查找商品列表失败: {}", name, e);
			return new ArrayList<>();
		}
	}

	// ==================== 数据解析 ====================

	private Map<String, String> parseSupplierInfo(String input) {
		Map<String, String> result = new HashMap<>();

		// 提取手机号
		java.util.regex.Pattern phonePattern = java.util.regex.Pattern.compile("1[3-9]\\d{9}");
		java.util.regex.Matcher phoneMatcher = phonePattern.matcher(input);
		if (phoneMatcher.find()) {
			result.put("phone", phoneMatcher.group());
			input = input.replaceAll(phoneMatcher.group(), "").trim();
		}

		// 提取联系人姓名（2-4个汉字）
		java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile("[\\u4e00-\\u9fa5]{2,4}");
		java.util.regex.Matcher nameMatcher = namePattern.matcher(input);
		if (nameMatcher.find()) {
			String name = nameMatcher.group();
			if (!name.contains("市") && !name.contains("区") && !name.contains("路") && !name.contains("省")) {
				result.put("contact", name);
				input = input.replace(name, "").trim();
			}
		}

		// 提取银行和账号
		java.util.regex.Pattern bankPattern = java.util.regex.Pattern.compile("(建行|工行|农行|中行|招行|交行).{0,10}?\\d{10,20}");
		java.util.regex.Matcher bankMatcher = bankPattern.matcher(input);
		if (bankMatcher.find()) {
			String bankInfo = bankMatcher.group();
			result.put("bankName", bankInfo.replaceAll("\\d+", "").trim());
			result.put("accountNo", bankInfo.replaceAll("[^0-9]", ""));
			input = input.replace(bankInfo, "").trim();
		}

		// 提取税号
		java.util.regex.Pattern taxPattern = java.util.regex.Pattern.compile("[A-Z0-9]{15,20}");
		java.util.regex.Matcher taxMatcher = taxPattern.matcher(input);
		if (taxMatcher.find()) {
			result.put("taxNo", taxMatcher.group());
		}

		// 剩余作为地址
		if (!input.isEmpty()) {
			result.put("address", input);
		}

		return result;
	}

	private Map<String, Object> parseGoodsInput(String input) {
		Map<String, Object> result = new HashMap<>();
		input = input.trim().replaceAll("\\s+", " ");

		// 去掉常见的前缀词（语音输入可能带有）
		input = input.replaceAll("^(商品[：:]?|名称[：:]?|货品[：:]?|物品[：:]?)\\s*", "").trim();
		input = cleanSentenceStartVerbs(input);

		// 先尝试提取供应商
		String supplier = null;

		// 格式1: "向XXX采购" / "向XXX买" / "向XXX进货"
		java.util.regex.Pattern supplierPattern1 = java.util.regex.Pattern.compile("向\\s*([^，。,\\s采购买进]+)\\s*(采购|买|进货)");
		java.util.regex.Matcher supplierMatcher1 = supplierPattern1.matcher(input);
		if (supplierMatcher1.find()) {
			supplier = supplierMatcher1.group(1).trim();
			input = input.replace(supplierMatcher1.group(), "").trim();
		}

		// 格式2: "从XXX进货" / "从XXX采购"
		if (supplier == null) {
			java.util.regex.Pattern supplierPattern2 = java.util.regex.Pattern.compile("从\\s*([^，。,\\s采购进货]+)\\s*(进货|采购)");
			java.util.regex.Matcher supplierMatcher2 = supplierPattern2.matcher(input);
			if (supplierMatcher2.find()) {
				supplier = supplierMatcher2.group(1).trim();
				input = input.replace(supplierMatcher2.group(), "").trim();
			}
		}

		// 格式3: "跟XXX买" / "跟XXX采购"
		if (supplier == null) {
			java.util.regex.Pattern supplierPattern3 = java.util.regex.Pattern.compile("跟\\s*([^，。,\\s采购买]+)\\s*(买|采购)");
			java.util.regex.Matcher supplierMatcher3 = supplierPattern3.matcher(input);
			if (supplierMatcher3.find()) {
				supplier = supplierMatcher3.group(1).trim();
				input = input.replace(supplierMatcher3.group(), "").trim();
			}
		}

		// 格式4: "供应商XXX" 或 "供应商：XXX"
		if (supplier == null) {
			java.util.regex.Pattern supplierPattern4 = java.util.regex.Pattern.compile("供应商[是为：:]*\\s*([^，。,\\n]+)");
			java.util.regex.Matcher supplierMatcher4 = supplierPattern4.matcher(input);
			if (supplierMatcher4.find()) {
				supplier = supplierMatcher4.group(1).trim();
				input = input.replace(supplierMatcher4.group(), "").trim();
			}
		}

		// 格式5: 句号/逗号后面的内容作为供应商
		if (supplier == null) {
			java.util.regex.Pattern supplierPattern5 = java.util.regex.Pattern.compile("[。，,]\\s*([\\u4e00-\\u9fa5a-zA-Z]{2,15})\\s*$");
			java.util.regex.Matcher supplierMatcher5 = supplierPattern5.matcher(input);
			if (supplierMatcher5.find()) {
				String potentialSupplier = supplierMatcher5.group(1).trim();
				// 排除常见的单位词
				if (!potentialSupplier.matches("(元|块|件|箱|个|台|瓶|包|袋|盒|套|条|斤|kg|克)")) {
					supplier = potentialSupplier;
					input = input.substring(0, supplierMatcher5.start()).trim();
				}
			}
		}

		if (supplier != null && !supplier.isEmpty()) {
			result.put("supplier", supplier);
		}

		// 去掉末尾的标点符号
		input = input.replaceAll("[。.!！?？，,、]+$", "").trim();

		// 提取价格 - 支持多种格式：8000元、单价8000元、单价是8000元、价格8000元
		BigDecimal price = null;
		java.util.regex.Pattern pricePattern = java.util.regex.Pattern.compile("(单价[是为]?|价格[是为]?)?\\s*(\\d+\\.?\\d*)\\s*[元块]");
		java.util.regex.Matcher priceMatcher = pricePattern.matcher(input);
		if (priceMatcher.find()) {
			try {
				price = new BigDecimal(priceMatcher.group(2));
				result.put("price", price);
				input = input.replace(priceMatcher.group(), "").trim();
			} catch (NumberFormatException e) {
				log.warn("解析价格失败: {}", priceMatcher.group(2));
			}
		}

		// 提取数量+单位
		Integer quantity = null;
		String unit = null;
		java.util.regex.Pattern quantityPattern = java.util.regex.Pattern.compile("(买|购|进|入|出|卖|采购|进货|入库|出库)?\\s*(\\d+)\\s*([箱瓶个件台套条斤kg克Lml包袋盒支把张本册]{1,5})");
		java.util.regex.Matcher quantityMatcher = quantityPattern.matcher(input);
		if (quantityMatcher.find()) {
			try {
				quantity = Integer.parseInt(quantityMatcher.group(2));
				unit = quantityMatcher.group(3);
				result.put("quantity", quantity);
				result.put("unit", unit);
				input = input.replace(quantityMatcher.group(), "").trim();
			} catch (NumberFormatException e) {
				log.warn("解析数量失败: {}", quantityMatcher.group(2));
			}
		}

		// 再次清理可能残留的标点和关键词
		input = input.replaceAll("[。.!！?？，,、]+$", "").trim();
		input = input.replaceAll("^[。.!！?？，,、]+", "").trim();
		// 清理可能残留的"单价是"等关键词
		input = input.replaceAll("(单价[是为]?|价格[是为]?)\\s*$", "").trim();

		// 剩余部分作为商品名称
		if (!input.isEmpty()) {
			String cleanedName = cleanGoodsName(input);
			if (isValidGoodsName(cleanedName)) {
				result.put("name", cleanedName);
			}
		}

		log.info("解析商品输入结果: {}", result);


		// 强校验商品名称必须存在
		String goodsName = (String) result.get("name");
		if (goodsName == null || goodsName.isEmpty()) {
			result.put("error", "您的描述中缺少商品信息。\n\n请重新提供完整信息。");
			return result;
		}


		// 强校验：一句话采购入库必须包含供应商信息
		if (supplier == null || supplier.isEmpty()) {
			result.put("error", "您的描述中缺少供应商信息。\n\n请重新提供完整信息。");
			return result;
		}

		// 强校验：一句话采购入库必须同时包含数量和价格
		quantity = (Integer) result.get("quantity");
		price = (BigDecimal) result.get("price");


		if (quantity == null && price == null) {
			// 数量和价格都缺少
			result.put("error", "您的描述中缺少商品数量和价格信息。\n\n请重新提供完整信息。");
		} else if (quantity == null) {
			// 只缺少数量
			result.put("error", "您的描述中缺少商品数量信息。\n\n请重新提供完整信息。");
		} else if (price == null) {
			// 只缺少价格
			result.put("error", "您的描述中缺少商品价格信息。\n\n请重新提供完整信息。");
		}
		return result;
	}

	private Map<String, String> parseNewGoodsInfo(String input) {
		Map<String, String> result = new HashMap<>();
		input = input.trim();

		// 提取条形码
		java.util.regex.Pattern barcodePattern = java.util.regex.Pattern.compile("\\d{8,20}");
		java.util.regex.Matcher barcodeMatcher = barcodePattern.matcher(input);
		if (barcodeMatcher.find()) {
			result.put("barcode", barcodeMatcher.group());
			input = input.replace(barcodeMatcher.group(), "").trim();
		}

		// 提取价格
		java.util.regex.Pattern pricePattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)\\s*[元元]");
		java.util.regex.Matcher priceMatcher = pricePattern.matcher(input);
		int priceIndex = 0;
		while (priceMatcher.find()) {
			if (priceIndex == 0) {
				result.put("purchasePrice", priceMatcher.group(1));
			} else {
				result.put("salePrice", priceMatcher.group(1));
			}
			priceIndex++;
		}

		// 移除价格信息
		input = input.replaceAll("\\d+\\.?\\d*\\s*[元元]", "").trim();

		// 提取单位
		java.util.regex.Pattern unitPattern = java.util.regex.Pattern.compile("(箱|瓶|个|包|袋|盒|台|套|条|斤|kg|克|L|ml|件)");
		java.util.regex.Matcher unitMatcher = unitPattern.matcher(input);
		if (unitMatcher.find()) {
			result.put("unit", unitMatcher.group());
			input = input.replace(unitMatcher.group(), "").trim();
		}

		// 剩余作为规格
		if (!input.isEmpty()) {
			result.put("spec", input);
		}

		return result;
	}

	private Integer parseQuantity(String input) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*[箱瓶个件台套条斤kg克]+");
		java.util.regex.Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		pattern = java.util.regex.Pattern.compile("\\b(\\d+)\\b");
		matcher = pattern.matcher(input);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return null;
	}

	private BigDecimal parsePrice(String input) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[¥￥]?(\\d+\\.?\\d*)\\s*元?");
		java.util.regex.Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			try {
				return new BigDecimal(matcher.group(1));
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	private String parseDeliveryDate(String input) {
		// 简单日期解析
		if (input.contains("明天")) {
			return LocalDate.now().plusDays(1).format(DATE_FORMATTER);
		} else if (input.contains("后天")) {
			return LocalDate.now().plusDays(2).format(DATE_FORMATTER);
		} else if (input.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
			return input;
		}
		return null;
	}

	private Map<String, Object> buildGoodsMap(WmsGoodsSkuRespVO sku, Map<String, Object> goodsInfo) {
		Map<String, Object> map = new HashMap<>();
		map.put("skuId", sku.getId());
		map.put("goodsId", sku.getGoodsId());
		map.put("name", sku.getSkuName());
		map.put("spec", sku.getSpecInfo());
		map.put("unit", sku.getUnitName());
		map.put("isNew", false);

		Integer quantity = (Integer) goodsInfo.get("quantity");
		if (quantity != null) {
			map.put("quantity", quantity);
		}

		BigDecimal price = (BigDecimal) goodsInfo.get("price");
		if (price != null) {
			map.put("price", price);
		} else if (sku.getPurchasePrice() != null) {
			map.put("price", sku.getPurchasePrice());
		}

		return map;
	}

	private ChatResponseVO reviewItems(ConversationState state) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");

		if (items.isEmpty()) {
			state.setCurrentStep(STEP_GOODS);
			return response(state, "还没有添加任何商品。请告诉我：**要采购什么商品？**");
		}
		return askMoreGoods(state);
	}

	// ==================== 判断方法 ====================

	private boolean isConfirm(String message) {
		String cleanMsg = cleanPunctuation(message);
		return "确认".equals(cleanMsg) || "yes".equalsIgnoreCase(cleanMsg)
				|| "好的".equals(cleanMsg) || "行".equals(cleanMsg);
	}

	private boolean isYes(String message) {
		String cleanMsg = cleanPunctuation(message);
		return "是".equals(cleanMsg) || "yes".equalsIgnoreCase(cleanMsg)
				|| "是的".equals(cleanMsg) || "对".equals(cleanMsg);
	}

	private boolean isCancel(String message) {
		String cleanMsg = cleanPunctuation(message);
		return "取消".equals(cleanMsg) || "不".equals(cleanMsg) || "no".equalsIgnoreCase(cleanMsg)
				|| "否".equals(cleanMsg);
	}

	/**
	 * 清理语音识别可能添加的标点符号
	 */
	private String cleanPunctuation(String message) {
		if (message == null) return "";
		return message.replaceAll("[。.!！?？，,、]+$", "").trim();
	}

	private String cleanSentenceStartVerbs(String input) {
		if (input == null) {
			return "";
		}
		String cleaned = input;
		String[] sentenceStartVerbs = {"采购入库", "进货入库", "入库", "采购", "进货", "买", "购", "入"};
		for (String verb : sentenceStartVerbs) {
			cleaned = cleaned.replaceAll("^" + verb + "\\s*", "");
		}
		return cleaned.trim();
	}

	private String cleanGoodsName(String goodsName) {
		if (goodsName == null) {
			return "";
		}
		String cleaned = goodsName.trim();
		String[] verbs = {"采购", "进货", "入库", "买", "购", "入", "购买", "购入"};
		for (String verb : verbs) {
			cleaned = cleaned.replaceAll("^" + verb + "\\s*", "");
		}
		cleaned = cleaned.replaceAll("^[。.!！?？，,、]+", "").replaceAll("[。.!！?？，,、]+$", "").trim();
		return cleaned;
	}

	private Map<String, Object> parseCreatedGoodsPayload(String json) {
		try {
			Map<String, Object> payload = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
			});
			if (payload != null && payload.get("data") instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String, Object>) payload.get("data");
				return data;
			}
			return payload != null ? payload : new HashMap<>();
		} catch (Exception e) {
			log.warn("解析 goods_created 回调失败: {}", json, e);
			return new HashMap<>();
		}
	}

	private Long toLong(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return Long.parseLong(value.toString());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 验证商品名称是否有效
	 * 至少2个字符，不能全是特殊符号
	 */
	private boolean isValidGoodsName(String name) {
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		String trimmed = name.trim();
		if (trimmed.length() < 2) {
			return false;
		}
		// 检查是否全是特殊符号
		String specialChars = "？?！!～@#$%^&*()_+-=[]{}|;:'\",.<>/?`~";
		for (char c : specialChars.toCharArray()) {
			if (trimmed.equals(String.valueOf(c))) {
				return false;
			}
		}
		return true;
	}

	private String nvl(String value, String defaultValue) {
		return (value == null || value.isEmpty()) ? defaultValue : value;
	}

	/**
	 * 跳转到修改页面（移动端）
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO navigateToModifyPage(ConversationState state) {
		log.info("跳转到移动端修改页面");
		
		// 收集当前订单数据
		Map<String, Object> orderData = new HashMap<>();
		orderData.put("supplierId", state.getData("supplierId"));
		orderData.put("supplierName", state.getData("supplierName"));
		orderData.put("warehouseId", state.getData("warehouseId"));
		orderData.put("warehouseName", state.getData("warehouseName"));
		orderData.put("inDate", state.getData("inDate"));
		orderData.put("remark", state.getData("remark"));
		orderData.put("items", state.getData("items"));
		orderData.put("conversationId", state.getConversationId());
		orderData.put("userId", state.getUserId());
		
		// 生成临时数据 key（使用 conversationId）
		String tempDataKey = "temp_purchase_order_" + state.getConversationId();
		
		// 将数据保存到 Redis（过期时间 30 分钟）
		try {
			RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
			redisCache.setCacheObject(tempDataKey, orderData, 30, TimeUnit.MINUTES);
			log.info("临时订单数据已保存到 Redis: {}", tempDataKey);
		} catch (Exception e) {
			log.error("保存临时订单数据失败：{}", e.getMessage());
		}
		
		// 返回跳转指令
		ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
		action.setName("navigateTo");
		action.setDescription("跳转到采购入库修改页面");
		Map<String, Object> args = new HashMap<>();
		args.put("url", "/subpackages/purchase/in-add");
		args.put("fromChat", true);
		args.put("tempDataKey", tempDataKey);
		action.setArguments(args);
		
		return response(state, "好的，已跳转到采购入库修改页面。\n\n- 当前订单数据已自动带入\n- 修改并保存后，数据将同步回聊天界面")
				.suggestedAction(action);
	}
}
