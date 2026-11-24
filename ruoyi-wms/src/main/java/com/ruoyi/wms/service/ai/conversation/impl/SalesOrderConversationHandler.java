package com.ruoyi.wms.service.ai.conversation.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.ai.chat.ChatRequestVO;
import com.ruoyi.wms.domain.vo.ai.chat.ChatResponseVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.service.WmsCustomerService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsWarehouseService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.service.ai.conversation.ConversationHandler;
import com.ruoyi.wms.service.ai.conversation.ConversationState;
import com.ruoyi.wms.service.ai.parser.ParsedOrderResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 销售订单对话处理器
 *
 * 完整对话流程：
 * 1. 客户（不存在则引导创建）→ 2. 商品（可循环添加，不存在则引导创建SKU）→ 3. 订单其他信息 → 4. 确认 → 完成
 *
 * @author ruoyi
 */
@Component
public class SalesOrderConversationHandler implements ConversationHandler {

	private static final Logger log = LoggerFactory.getLogger(SalesOrderConversationHandler.class);

	@Autowired
	private WmsCustomerService customerService;

	@Autowired
	private WmsGoodsService goodsService;

	@Autowired
	private WmsGoodsSkuService goodsSkuService;

	@Autowired
	private WmsWarehouseService warehouseService;

	@Autowired
	private com.ruoyi.wms.service.WmsSalesOutService salesOrderService;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String ERR_AI_GOODS_CREATE_FAILED = "AI_GOODS_CREATE_FAILED";
	private static final String KEY_PENDING_GOODS_INPUT = "pendingGoodsInput";
	private static final String KEY_PENDING_GOODS_INFO = "pendingGoodsInfo";

	// 对话步骤枚举
	private static final String STEP_CUSTOMER = "customer";
	private static final String STEP_CUSTOMER_SELECT = "customer_select";
	private static final String STEP_CUSTOMER_NEW_CONFIRM = "customer_new_confirm";
	private static final String STEP_CUSTOMER_NEW = "customer_new";
	private static final String STEP_CUSTOMER_CONFIRM = "customer_confirm";
	private static final String STEP_GOODS = "goods";
	private static final String STEP_GOODS_SELECT = "goods_select";
	private static final String STEP_GOODS_NEW = "goods_new";
	private static final String STEP_GOODS_NEW_CONFIRM = "goods_new_confirm";
	private static final String STEP_GOODS_CREATE_PAGE = "goods_create_page";
	private static final String STEP_GOODS_CONFIRM = "goods_confirm";
	private static final String STEP_GOODS_QUANTITY = "goods_quantity";
	private static final String STEP_GOODS_PRICE = "goods_price";
	private static final String STEP_MORE_GOODS = "more_goods";
	private static final String STEP_ORDER_INFO = "order_info";
	private static final String STEP_CONFIRM_ORDER = "confirm_order";

	@Override
	public String getOperationType() {
		return "create_sales_out";
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

		// 获取常用客户列表，供后续使用
		List<WmsCustomer> customers = customerService.list(
				Wrappers.<WmsCustomer>lambdaQuery()
						.orderByDesc(WmsCustomer::getIsDefault)
						.orderByDesc(WmsCustomer::getCreateTime)
						.last("LIMIT 3")
		);
		if (customers != null && !customers.isEmpty()) {
			state.setData("availableCustomers", customers);
		}

		StringBuilder reply = new StringBuilder();
		reply.append("好的，我来帮您创建**销售出库单**。\n\n");
		reply.append("💡 系统会自动识别，找不到的会帮您新建\n");
		reply.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

		// 示例提示词列表
		List<String> examplePrompts = Arrays.asList(
			"卖给天迅科技有限公司100台惠普笔记本，售价5000元。",
			"出货给华联超市50箱可乐，每箱80元。",
			"给京东客户发200个无线鼠标，单价60元。"
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

		log.info("销售出库对话 - 步骤: {}, 输入: {}", step, message);

		if (message.startsWith("customer_created:")) {
			return handleCustomerCreatedCallback(state, message);
		}
		if (message.startsWith("goods_created:") || message.startsWith("goods_create_failed:")) {
			return handleGoodsCreatePage(state, message);
		}

		switch (step) {
			case STEP_CUSTOMER:
				return handleCustomer(state, message);
			case STEP_CUSTOMER_SELECT:
				return handleCustomerSelect(state, message);
			case STEP_CUSTOMER_NEW_CONFIRM:
				return handleCustomerNewConfirm(state, message);
			case STEP_CUSTOMER_NEW:
				return handleCustomerNew(state, message);
			case STEP_CUSTOMER_CONFIRM:
				return handleCustomerConfirm(state, message);
			case STEP_GOODS:
				return handleGoods(state, message);
			case STEP_GOODS_SELECT:
				return handleGoodsSelect(state, message);
			case STEP_GOODS_NEW:
				return handleGoodsNew(state, message);
			case STEP_GOODS_NEW_CONFIRM:
				return handleGoodsNewConfirm(state, message);
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

	// ==================== 客户相关 ====================

	private ChatResponseVO askForCustomer(ConversationState state) {
		List<WmsCustomer> customers = (List<WmsCustomer>) state.getData("availableCustomers");
		StringBuilder sb = new StringBuilder();
		sb.append("**请选择或输入客户**：\n");

		if (customers != null && !customers.isEmpty()) {
			sb.append("\n📋 常用客户（输入关键字即可）：\n");
			for (int i = 0; i < customers.size(); i++) {
				WmsCustomer c = customers.get(i);
				sb.append("\n").append(i + 1).append(". ").append(c.getCustomerName());
			}
		} else {
			sb.append("\n💡 直接输入客户名称，系统会自动创建");
		}

		return response(state, sb.toString());
	}

	private ChatResponseVO handleCustomer(ConversationState state, String message) {
		// 检查用户是否在确认新建客户
		if (state.hasData("tempCustomerName")) {
			if (isYes(message)) {
				// 直接记录新客户
				state.setData("customerId", null);
				state.setData("customerName", state.getData("tempCustomerName"));
				state.setData("isNewCustomer", true);
				state.removeData("tempCustomerName");
				state.setCurrentStep(STEP_ORDER_INFO);

				return response(state, "✅ 客户【" + state.getData("customerName") + "】已记录。\n\n" + askOrderInfo(state).getReply());
			} else if (isCancel(message)) {
				// 用户不想创建，清除临时数据并重新显示客户列表
				state.removeData("tempCustomerName");
				return responseWithCustomerList(state, "请重新选择**客户**（输入序号或客户名称）：");
			}
		}

		// 查找客户
		WmsCustomer customer = findCustomer(message);

		if (customer != null) {
			// 找到客户，先保存，让用户确认
			state.setData("tempSelectedCustomerId", customer.getId());
			state.setData("tempSelectedCustomerName", customer.getCustomerName());
			state.setCurrentStep(STEP_CUSTOMER_SELECT);
			return response(state, "找到客户：**" + customer.getCustomerName() + "**\n\n" +
					"请确认：\n" +
					"- 回答\"是\"或\"确认\"选择此客户\n" +
					"- 重新输入其他客户名称").withYesNoReplies();
		}

		// 没找到客户，询问是否新建
		state.setData("tempCustomerName", message);
		return response(state, "⚠️ 没有找到客户【" + message + "】\n\n" +
				"这是一个**新客户**吗？\n" +
				"- 回答\"是\"，我将记录这个客户\n" +
				"- 回答\"否\"，重新选择客户").withYesNoReplies();
	}

	private ChatResponseVO handleCustomerSelect(ConversationState state, String message) {
		// 用户确认选择此客户
		if (isYes(message) || isConfirm(message)) {
			state.setData("customerId", state.getData("tempSelectedCustomerId"));
			state.setData("customerName", state.getData("tempSelectedCustomerName"));
			state.removeData("tempSelectedCustomerId");
			state.removeData("tempSelectedCustomerName");
			state.setCurrentStep(STEP_ORDER_INFO);

			return response(state, "✅ 已选择客户：" + state.getData("customerName") +
					"\n\n" + askOrderInfo(state).getReply());
		}

		// 用户不想选择此客户，重新搜索
		state.removeData("tempSelectedCustomerId");
		state.removeData("tempSelectedCustomerName");
		state.setCurrentStep(STEP_CUSTOMER);
		return responseWithCustomerList(state, "请重新选择**客户**（输入序号或客户名称）：");
	}

	private ChatResponseVO handleCustomerNewConfirm(ConversationState state, String message) {
		state.setData("customerId", null);
		state.setData("customerName", state.getData("tempCustomerName"));
		state.setData("isNewCustomer", true);
		state.removeData("tempCustomerName");
		state.setCurrentStep(STEP_ORDER_INFO);

		return response(state, "✅ 客户【" + state.getData("customerName") + "】已记录。\n\n" );
	}

	private ChatResponseVO handleCustomerNew(ConversationState state, String message) {
		state.setData("customerId", null);
		state.setData("customerName", state.getData("tempCustomerName"));
		state.setData("isNewCustomer", true);
		state.removeData("tempCustomerName");
		state.setCurrentStep(STEP_ORDER_INFO);

		return response(state, "✅ 客户【" + state.getData("customerName") + "】已记录。\n\n" + askOrderInfo(state).getReply());
	}

	private ChatResponseVO handleCustomerConfirm(ConversationState state, String message) {
		return null; // 不再需要确认步骤
	}

	// ==================== 商品相关 ====================

	private ChatResponseVO handleGoods(ConversationState state, String message) {
		if (isCancel(message)) {
			return reviewItems(state);
		}


		// 只在 STEP_GOODS 步骤时才检查新建确认
//		if (STEP_GOODS.equals(state.getCurrentStep()) && state.hasData("tempGoodsName") && isYes(message)) {
//			state.setCurrentStep(STEP_GOODS_NEW_CONFIRM);
//			return askGoodsInfo(state);
//		}

		// 验证输入 - 商品名称至少2个字符，且不能全是特殊符号
		if (!isValidGoodsName(message)) {
			return response(state, "请输入有效的**商品名称**（至少2个字符，不能全是符号）？");
		}

		// 解析商品信息
		Map<String, Object> goodsInfo = parseGoodsInput(message);

		// 检查解析错误（数量或价格缺失）
		String errorMsg = (String) goodsInfo.get("error");
		if (errorMsg != null && !errorMsg.isEmpty()) {
			// 构建快捷回复选项：重新创建
			List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
			quickReplies.add(new ChatResponseVO.QuickReply("重新创建", "重新创建销售出库", "primary"));
			
			return response(state, "❌ **创建订单失败**\n\n" + errorMsg + "\n\n请点击下方按钮或输入新的指令重新开始。")
					.withQuickReplies(quickReplies);
		}

		// 处理客户信息（如果解析结果中包含客户）
		String customer = (String) goodsInfo.get("customer");
		if (customer != null && !customer.isEmpty()) {
			// 查找客户
			WmsCustomer foundCustomer = findCustomer(customer);
			if (foundCustomer != null) {
				state.setData("customerId", foundCustomer.getId());
				state.setData("customerName", foundCustomer.getCustomerName());
				state.setData("isNewCustomer", false);
				log.info("从商品描述中提取到客户：{}", foundCustomer.getCustomerName());
			} else {
				// 未找到客户，标记为新客户
				state.setData("customerId", null);
				state.setData("customerName", customer);
				state.setData("isNewCustomer", true);
				log.info("从商品描述中提取到新客户：{}", customer);
			}
		}
		String goodsName = (String) goodsInfo.get("name");

		// 查找商品列表
		List<WmsGoodsSkuRespVO> skuList = findGoodsList(goodsName);

		if (skuList != null && !skuList.isEmpty()) {
			// 找到商品，处理商品匹配
			return handleGoodsMatch(state, goodsInfo, skuList);
		} else {
			// 未找到商品，构建快捷回复选项：重新创建
			List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
			quickReplies.add(new ChatResponseVO.QuickReply("重新创建", "重新创建销售出库", "primary"));

			return response(state, "❌ **商品未录入**\n\n商品【" + goodsName + "】未录入商品库，无法进行销售出库操作。\n\n请点击下方按钮或重新整理一句话创建销售出库订单有效信息。")
					.withQuickReplies(quickReplies);
		}
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
				if (sku.getSalePrice() != null) {
					sb.append(" - 零售价：¥").append(sku.getSalePrice());
				}
				if (sku.getWholesalePrice() != null && sku.getWholesalePrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
					sb.append(" / 批发价：¥").append(sku.getWholesalePrice());
				}
				sb.append("\n");
			}

			sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
			sb.append("请输入序号选择商品");

			// 构建快捷回复选项
			List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
			for (int i = 0; i < skuList.size(); i++) {
				quickReplies.add(new ChatResponseVO.QuickReply((i + 1) + "", String.valueOf(i + 1), "default"));
			}

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
		String unit = (String) goodsInfo.get("unit");

		// 如果没有单位，使用商品的单位
		if (unit == null || unit.isEmpty()) {
			unit = sku.getUnitName() != null ? sku.getUnitName() : "件";
		}

		// 计算金额
		BigDecimal amount = price.multiply(new BigDecimal(quantity));

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

		// 直接显示订单确认页面
		state.setCurrentStep(STEP_CONFIRM_ORDER);
		return showOrderConfirm(state);
	}

	/**
	 * 处理商品选择（多个匹配时）
	 */
	private ChatResponseVO handleGoodsSelect(ConversationState state, String message) {
		List<WmsGoodsSkuRespVO> skuList = (List<WmsGoodsSkuRespVO>) state.getData("tempMatchedGoodsList");
		Map<String, Object> goodsInfo = (Map<String, Object>) state.getData("tempGoodsInfo");

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
				if (sku.getSalePrice() != null) {
					sb.append(" - 零售价：¥").append(sku.getSalePrice());
				}
				if (sku.getWholesalePrice() != null && sku.getWholesalePrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
					sb.append(" / 批发价：¥").append(sku.getWholesalePrice());
				}
				sb.append("\n");
			}

			sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
			sb.append("请输入序号选择商品");

			return response(state, sb.toString());
		}

		// 出错，返回商品输入步骤
		state.setCurrentStep(STEP_GOODS);
		return response(state, "请重新输入**商品信息**？");
	}


	/**
	 * 处理商品创建页面相关操作
	 */
	private ChatResponseVO handleGoodsCreatePage(ConversationState state, String message) {
		if ("快速创建".equals(message) || "快速创建商品".equals(message)) {
			// 快速创建：静默创建商品，不需要跳转页面
			String goodsName = state.getData("tempGoodsName", "");
			Map<String, Object> goodsInfo = (Map<String, Object>) state.getData(KEY_PENDING_GOODS_INFO);
			return createGoodsDirectly(state, goodsName, goodsInfo);
		}

		if ("填写详情".equals(message) || "详情创建".equals(message) || "手动创建".equals(message)) {
			// 填写详情：跳转到商品创建页面
			return buildCreateGoodsActionResponse(state, false);
		}

		if (message.startsWith("goods_created:")) {
			String payloadJson = message.substring("goods_created:".length());
			try {
				Map<String, Object> payload = OBJECT_MAPPER.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
				});
				String goodsName = payload.get("goodsName") == null ? state.getData("tempGoodsName", "") : String.valueOf(payload.get("goodsName"));
				Long skuId = null;
				Object skuIdObj = payload.get("skuId");
				if (skuIdObj instanceof Number) {
					skuId = ((Number) skuIdObj).longValue();
				} else if (skuIdObj instanceof String && !((String) skuIdObj).trim().isEmpty()) {
					skuId = Long.parseLong((String) skuIdObj);
				}

				WmsGoodsSkuRespVO sku = null;
				if (skuId != null) {
					sku = goodsSkuService.get(skuId);
				}
				if (sku == null && goodsName != null && !goodsName.isEmpty()) {
					sku = findGoods(goodsName);
				}

				Map<String, Object> tempGoods = new HashMap<>();
				if (sku != null) {
					tempGoods = buildGoodsMap(sku, new HashMap<>());
				} else {
					tempGoods.put("name", goodsName);
					tempGoods.put("skuId", skuId);
					tempGoods.put("goodsId", payload.get("goodsId"));
					tempGoods.put("unit", payload.get("unit") == null ? "件" : String.valueOf(payload.get("unit")));
					tempGoods.put("spec", payload.get("spec") == null ? "" : String.valueOf(payload.get("spec")));
					tempGoods.put("isNew", true);
				}

				state.setData("currentGoods", tempGoods);
				state.removeData("tempGoodsName");
				state.setCurrentStep(STEP_GOODS_QUANTITY);
				String displayName = String.valueOf(tempGoods.getOrDefault("name", goodsName));
				return response(state, "✅ 商品已创建：" + displayName + "\n\n继续填写数量即可。\n" + askGoodsQuantity(state, displayName).getReply());
			} catch (Exception ex) {
				log.warn("解析 goods_created 回流失败", ex);
				return response(state, "已收到商品创建回流，但解析失败，请输入商品名称继续。\n\n错误信息：" + ex.getMessage());
			}
		}

		if (message.startsWith("goods_create_failed:")) {
			String error = message.substring("goods_create_failed:".length()).trim();
			if (error.isEmpty()) {
				error = "商品创建失败，请补全信息后重试。";
			}
			state.setCurrentStep(STEP_GOODS_CREATE_PAGE);
			return response(state, "⚠️ " + ERR_AI_GOODS_CREATE_FAILED + ":" + error + "\n\n可选择：\n- 继续在商品页完善后保存\n- 或直接告诉我新的商品名称，我帮你重新发起创建");
		}

		if ("商品创建完成".equals(message)) {
			// 用户从商品创建页面返回
			String goodsName = state.getData("tempGoodsName", "");
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
				tempGoods.put("unit", "件"); // 默认单位
				tempGoods.put("spec", ""); // 规格可以为空
				tempGoods.put("skuId", null);
				tempGoods.put("goodsId", null);
				state.setData("currentGoods", tempGoods);
				state.setCurrentStep(STEP_GOODS_QUANTITY);
				return askGoodsQuantity(state, goodsName);
			}

			// 没有商品名称，询问用户输入
			state.setCurrentStep(STEP_GOODS);
			return response(state, "✅ 已返回对话\n\n请告诉我：**要销售什么商品？**");
		}

		// 其他输入，重新尝试查找商品
		state.removeData("tempGoodsName");
		state.removeData(KEY_PENDING_GOODS_INFO);
		state.setCurrentStep(STEP_GOODS);
		return handleGoods(state, message);
	}

	@SuppressWarnings("unchecked")
	private ChatResponseVO buildCreateGoodsActionResponse(ConversationState state, boolean quickMode) {
		String goodsName = state.getData("tempGoodsName", "");
		Map<String, Object> goodsInfo = (Map<String, Object>) state.getData(KEY_PENDING_GOODS_INFO);

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("goodsName", goodsName);
		arguments.put("fromChat", "true");
		arguments.put("conversationId", state.getConversationId());
		arguments.put("createMode", quickMode ? "quick" : "detail");

		if (goodsInfo != null) {
			Object unit = goodsInfo.get("unit");
			Object price = goodsInfo.get("price");
			if (unit != null) {
				arguments.put("unit", String.valueOf(unit));
			}
			if (price instanceof BigDecimal) {
				arguments.put("salePrice", price.toString());
			}
		}

		ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
		action.setName("create_goods");
		action.setDescription("前往商品创建页面");
		action.setArguments(arguments);

		String reply = quickMode
				? "请点击「执行操作」快速创建商品，保存后将自动回流继续填写出库单。"
				: "请点击「执行操作」前往商品页填写详情，保存后将自动回流继续填写出库单。";

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
	 * 直接根据用户输入创建商品（无需页面操作）
	 * 根据用户一句话的信息（商品名称、单位、销售价格）直接创建商品
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO createGoodsDirectly(ConversationState state, String goodsName, Map<String, Object> goodsInfo) {
		try {
			log.info("开始直接创建商品: {}, 信息: {}", goodsName, goodsInfo);

			// 提取商品信息
			String spec = (String) goodsInfo.get("spec");
			String unit = (String) goodsInfo.get("unit");
			BigDecimal salePrice = (BigDecimal) goodsInfo.get("price");
			String barcode = (String) goodsInfo.get("barcode");

			// 创建商品主表
			com.ruoyi.wms.domain.vo.goods.WmsGoodsSaveReqVO goodsReqVO = new com.ruoyi.wms.domain.vo.goods.WmsGoodsSaveReqVO();
			goodsReqVO.setGoodsName(goodsName);
			goodsReqVO.setGoodsCode(goodsService.generateGoodsCode(null)); // 自动生成编码
			goodsReqVO.setHasSpec("0"); // 无规格
			goodsReqVO.setStatus("0"); // 正常状态
			
			// 获取默认分类ID（第一个分类）
			String defaultCategoryId = getDefaultCategoryId();
			if (defaultCategoryId != null) {
				goodsReqVO.setCategoryId(defaultCategoryId);
			}
			
			// 获取默认单位ID（如果指定了单位，尝试查找）
			if (unit != null && !unit.isEmpty()) {
				Long unitId = getUnitIdByName(unit);
				if (unitId != null) {
					goodsReqVO.setUnitId(unitId);
				}
			}
			
			Long goodsId = goodsService.create(goodsReqVO);
			log.info("商品创建成功, goodsId: {}", goodsId);

			// 创建SKU
			com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO skuReqVO = new com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO();
			skuReqVO.setGoodsId(goodsId);
			skuReqVO.setSkuCode(goodsService.generateSkuCode(goodsId)); // 自动生成SKU编码
			skuReqVO.setSkuName(goodsName);
			
			if (spec != null && !spec.isEmpty()) {
				skuReqVO.setSpecInfo(spec);
			}
			
			if (unit != null && !unit.isEmpty()) {
				Long unitId = getUnitIdByName(unit);
				if (unitId != null) {
					skuReqVO.setUnitId(unitId);
				}
			}
			
			if (barcode != null && !barcode.isEmpty()) {
				skuReqVO.setBarcode(barcode);
			}
			
			if (salePrice != null) {
				skuReqVO.setSalePrice(salePrice);
			}
			
			skuReqVO.setStatus("0"); // 正常状态
			
			Long skuId = goodsSkuService.create(skuReqVO);
			log.info("SKU创建成功, skuId: {}", skuId);

			// 获取刚创建的SKU详情
			WmsGoodsSkuRespVO sku = goodsSkuService.get(skuId);

			// 构建商品信息
			Map<String, Object> tempGoods = buildGoodsMap(sku, new HashMap<>());
			tempGoods.put("isNew", true);
			state.setData("currentGoods", tempGoods);

			log.info("商品创建完成: {}", sku.getSkuName());

			// 数量和价格都有，直接添加到订单中
			Integer quantity = (Integer) goodsInfo.get("quantity");
			BigDecimal price = (BigDecimal) goodsInfo.get("price");
			tempGoods.put("quantity", quantity);
			tempGoods.put("price", price);

			// 计算金额
			BigDecimal qty = new BigDecimal(quantity.toString());
			tempGoods.put("amount", qty.multiply(price));

			// 添加到商品列表
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
			items.add(tempGoods);
			state.setData("items", items);
			state.removeData("currentGoods");

			log.info("商品已添加到订单: {} x{} × ¥{}", sku.getSkuName(), quantity, price);

			// 直接进入确认订单流程
			state.setCurrentStep(STEP_CONFIRM_ORDER);
			return showOrderConfirm(state);

		} catch (Exception e) {
			log.warn("直接创建商品失败: {}", goodsName, e);
			state.setData(KEY_PENDING_GOODS_INFO, goodsInfo);
			state.setData("tempGoodsName", goodsName);
			state.setCurrentStep(STEP_GOODS_CREATE_PAGE);
			
			// 创建失败，返回引导用户手动创建
			return response(state, "⚠️ 商品创建失败：" + e.getMessage() + "\n\n" +
					"请前往商品创建页面手动创建。\n\n" +
					"可选择：\n- 点击「执行操作」前往商品页创建\n- 或告诉我新的商品信息");
		}
	}

	/**
	 * 获取默认分类ID
	 */
	private String getDefaultCategoryId() {
		try {
			// 这里需要根据实际情况获取默认分类ID
			// 暂时返回null，由系统自动生成
			return null;
		} catch (Exception e) {
			log.warn("获取默认分类ID失败", e);
			return null;
		}
	}

	/**
	 * 根据单位名称获取单位ID
	 */
	private Long getUnitIdByName(String unitName) {
		try {
			// 这里需要根据实际情况查询单位表
			// 暂时返回null，由系统处理
			return null;
		} catch (Exception e) {
			log.warn("根据单位名称获取单位ID失败: {}", unitName, e);
			return null;
		}
	}

	/**
	 * 处理商品确认
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO handleGoodsConfirm(ConversationState state, String message) {
		if ("是这个商品".equals(message) || (isYes(message) && !state.hasData("tempGoodsName"))) {
			// 用户确认是这个商品
			WmsGoodsSkuRespVO sku = (WmsGoodsSkuRespVO) state.getData("tempSelectedSku");
			Map<String, Object> goodsInfo = (Map<String, Object>) state.getData("tempGoodsInfo");

			if (sku != null) {
				state.setData("currentGoods", buildGoodsMap(sku, goodsInfo != null ? goodsInfo : new HashMap<>()));
				state.removeData("tempSelectedSku");
				state.removeData("tempGoodsInfo");
				return askGoodsQuantity(state, sku.getSkuName());
			}
		} else if ("创建新商品".equals(message) || isCancel(message)) {
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
		return response(state, "请重新告诉我：**要销售什么商品？**");
	}

	private ChatResponseVO handleGoodsNew(ConversationState state, String message) {
		if (!isYes(message)) {
			state.removeData("tempGoodsName");
			state.setCurrentStep(STEP_CUSTOMER);
			return responseWithCustomerList(state, "请重新选择**客户**（输入序号或客户名称）：");
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
		return response(state, "📦 " + name + "\n\n请输入**销售单价**（元）？\n\n💡 后续可输入 \"数量改XX\" 或 \"单价改XX\" 修改");
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
		String cleanMessage = message.replaceAll("[。.!！?？，,、]+$", "").trim();

		// 处理快捷回复
		if ("下一步".equals(cleanMessage) || "完成订单".equals(cleanMessage) || isConfirm(cleanMessage) || "完成".equals(cleanMessage)) {
			// 检查是否已选择客户
			if (!state.hasData("customerName")) {
				// 尝试自动选择默认客户
				WmsCustomer defaultCustomer = pickDefaultCustomer(state);
				if (defaultCustomer != null) {
					state.setData("customerId", defaultCustomer.getId());
					state.setData("customerName", defaultCustomer.getCustomerName());
				} else {
					state.setCurrentStep(STEP_CUSTOMER);
					return askForCustomer(state);
				}
			}
			state.setCurrentStep(STEP_ORDER_INFO);
			return askOrderInfo(state);
		}

		if ("继续添加".equals(cleanMessage)) {
			state.setCurrentStep(STEP_GOODS);
			return response(state, "请告诉我：**要销售什么商品？**");
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
			return response(state, "⚠️ 还没有添加任何商品！\n\n请告诉我：**要销售什么商品？**");
		}

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (Map<String, Object> item : items) {
			totalAmount = totalAmount.add(new BigDecimal(item.get("amount").toString()));
		}

		// 提取并保存客户信息（如果解析结果中包含客户）
		String customerName = (String) state.getData("customerName");
		if (customerName != null && !customerName.isEmpty()) {
			// 查找客户
			WmsCustomer customer = findCustomer(customerName);
			if (customer != null) {
				state.setData("customerId", customer.getId());
				state.setData("customerName", customer.getCustomerName());
				state.setData("isNewCustomer", false);
				log.info("从输入中提取到客户: {}", customer.getCustomerName());
			} else {
				// 未找到客户，直接创建新客户
				WmsCustomer newCustomer = new WmsCustomer();
				newCustomer.setCustomerName(customerName);
				newCustomer.setCustomerCode(generateCustomerCode());
				newCustomer.setCustomerType("2"); // 默认零售客户
				newCustomer.setStatus("0"); // 正常状态
				try {
					customerService.save(newCustomer);
					state.setData("customerId", newCustomer.getId());
					state.setData("customerName", newCustomer.getCustomerName());
					state.setData("isNewCustomer", true);
					log.info("自动创建新客户: {}，ID: {}", customerName, newCustomer.getId());
				} catch (Exception e) {
					log.info("自动创建客户失败，转报错信息: {}", customerName);
					return response(state, "⚠️ 创建新客户失败，请联系管理员。");
				}
			}
		}

		// 用户未指定客户时，使用默认客户
		if (state.getData("customerId") == null && (state.getData("customerName") == null || ((String) state.getData("customerName")).isEmpty())) {
			WmsCustomer defaultCustomer = customerService.getDefault();
			if (defaultCustomer != null) {
				state.setData("customerId", defaultCustomer.getId());
				state.setData("customerName", defaultCustomer.getCustomerName());
				state.setData("isNewCustomer", false);
				log.info("使用默认客户: {}", defaultCustomer.getCustomerName());
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("📋 **销售出库确认**\n\n");
		sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

		// 客户信息
		sb.append("👤 客户：").append(state.getData("customerName"));
		if (Boolean.TRUE.equals(state.getData("isNewCustomer"))) {
			sb.append("（新客户*）");
		}
		sb.append("\n");

		// 统一摘要顺序：客户 -> 仓库 -> 出库日期 -> 商品 -> 数量 -> 单价 -> 金额
		String warehouseName = state.getData("warehouseName", "默认仓库");
		sb.append("🏠 仓库：").append(warehouseName).append("\n");
		String orderDate = LocalDate.now().format(DATE_FORMATTER);
		sb.append("📅 出库日期：").append(orderDate).append("\n");

		String deliveryDate = (String) state.getData("deliveryDate");
		if (deliveryDate != null) {
			sb.append("📦 交货日期：").append(deliveryDate).append("\n");
		}

		String handler = (String) state.getData("handler");
		if (handler != null) {
			sb.append("👤 经办人：").append(handler).append("\n");
		}

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

		sb.append("\n💰 总金额：¥").append(totalAmount).append("\n");

		String remark = (String) state.getData("remark");
		if (remark != null && !remark.isEmpty()) {
			sb.append("\n\n📝 备注：").append(remark);
		}

		sb.append("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
		sb.append("请确认订单信息：");
		if (Boolean.TRUE.equals(state.getData("isNewCustomer")) ||
				items.stream().anyMatch(i -> Boolean.TRUE.equals(i.get("isNew")))) {
			sb.append("\n\n* 标记为新建的项目将一并创建");
		}

		// 构建快捷回复选项 - 确认按钮和修改订单按钮
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

		// 处理修改指令 - 支持 "修改数量为100", "把单价改成50", "客户换成XX" 等格式
//		if (message.contains("修改") || message.contains("改成") || message.contains("换成") || message.contains("改为")) {
//			return handleModifyOrder(state, message);
//		}

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

		// 解析并更新客户： "客户换成XX公司" / "修改客户为YY"
		if (message.contains("客户") && (message.contains("换成") || message.contains("改为") || message.contains("为"))) {
			// 提取客户名称
			String newCustomerName = extractCustomerName(message);
			if (newCustomerName != null && !newCustomerName.isEmpty()) {
				// 查找客户
				WmsCustomer customer = findCustomer(newCustomerName);
				if (customer != null) {
					state.setData("customerId", customer.getId());
					state.setData("customerName", customer.getCustomerName());
					return response(state, "✅ 客户已更新为：" + newCustomerName + "，金额已按当前数量与单价重算。\n\n" + getCurrentOrderSummary(state) + "\n\n是否确认创建？").withQuickReplies(buildConfirmQuickReplies());
				} else {
					// 未找到客户，直接创建新客户
					WmsCustomer newCustomer = new WmsCustomer();
					newCustomer.setCustomerName(newCustomerName);
					newCustomer.setCustomerCode(generateCustomerCode());
					newCustomer.setCustomerType("2"); // 默认零售客户
					newCustomer.setStatus("0"); // 正常状态
					try {
						customerService.save(newCustomer);
						state.setData("customerId", newCustomer.getId());
						state.setData("customerName", newCustomer.getCustomerName());
						state.setData("isNewCustomer", true);
						log.info("自动创建新客户: {}，ID: {}", newCustomerName, newCustomer.getId());
						return response(state, "✅ 客户【" + newCustomerName + "】已自动创建并更新，金额已重算。\n\n" + getCurrentOrderSummary(state) + "\n\n是否确认创建？").withQuickReplies(buildConfirmQuickReplies());
					} catch (Exception e) {
						log.warn("自动创建客户失败: {}", newCustomerName, e);
						state.setData("tempCustomerName", newCustomerName);
						state.setData("isNewCustomer", true);
						return buildCreateCustomerActionResponse(state, newCustomerName,
								"自动创建客户失败，请手动创建客户。\n\n请点击「执行操作」先创建客户，保存后将自动回流继续更新订单。");
					}
				}
			}
		}

		// 无法识别的修改指令
		return response(state, "抱歉，我没有理解您的修改意图。\n\n您可以说：\n" +
				"- \"数量改成100\"\n" +
				"- \"单价改成50\"\n" +
				"- \"客户换成XX公司\"\n\n" +
				getCurrentOrderSummary(state) + "\n\n是否确认创建？").withQuickReplies(buildConfirmQuickReplies());
	}

	/**
	 * 提取客户名称
	 */
	private String extractCustomerName(String message) {
		// 匹配 "客户换成XXX" 或 "修改客户为XXX" 或 "客户改为XXX"
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("客户[换为改]*([^，。！？\\n]+)");
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
		sb.append("客户：").append(state.getData("customerName")).append("\n");
		sb.append("仓库：").append(state.getData("warehouseName", "默认仓库")).append("\n");
		sb.append("出库日期：").append(LocalDate.now().format(DATE_FORMATTER)).append("\n");

		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
		if (!items.isEmpty()) {
			Map<String, Object> item = items.get(0);
			sb.append("商品：").append(item.get("name"));
			sb.append("（").append(item.get("unit")).append("）");
			sb.append(" x ").append(item.get("quantity"));
			sb.append(" @ ¥").append(item.get("price"));
			sb.append("\n金额：¥").append(item.get("amount"));
		}
		return sb.toString();
	}

	private ChatResponseVO handleCustomerCreatedCallback(ConversationState state, String message) {
		String payloadJson = message.substring("customer_created:".length());
		try {
			Map<String, Object> payload = OBJECT_MAPPER.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
			});
			Object idObj = payload.get("id");
			Object nameObj = payload.get("name");
			if (idObj instanceof Number) {
				state.setData("customerId", ((Number) idObj).longValue());
				state.setData("isNewCustomer", false);
			}
			if (nameObj != null) {
				state.setData("customerName", String.valueOf(nameObj));
			}

			String pendingGoodsInput = state.getData(KEY_PENDING_GOODS_INPUT, "");
			if (pendingGoodsInput != null && !pendingGoodsInput.isEmpty()) {
				state.removeData(KEY_PENDING_GOODS_INPUT);
				state.setCurrentStep(STEP_GOODS);
				return handleGoods(state, pendingGoodsInput);
			}

			if (STEP_CONFIRM_ORDER.equals(state.getCurrentStep())) {
				return response(state, "✅ 客户创建回流成功，金额已重算，请确认订单。\n\n" + getCurrentOrderSummary(state) + "\n\n是否确认创建？")
					.withQuickReplies(buildConfirmQuickReplies());
			}

			state.setCurrentStep(STEP_ORDER_INFO);
			return response(state, "✅ 客户创建回流成功：" + state.getData("customerName", "") + "\n\n" + askOrderInfo(state).getReply());
		} catch (Exception ex) {
			log.warn("解析 customer_created 回流失败", ex);
			return response(state, "已收到客户创建回流，但解析失败，请直接输入客户名称继续。");
		}
	}

	private ChatResponseVO buildCreateCustomerActionResponse(ConversationState state, String customerName, String reply) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("name", customerName);
		arguments.put("fromChat", "true");
		arguments.put("conversationId", state.getConversationId());

		ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
		action.setName("create_customer");
		action.setDescription("前往客户创建页面");
		action.setArguments(arguments);

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
	 * 构建确认快捷回复
	 */
	private List<ChatResponseVO.QuickReply> buildConfirmQuickReplies() {
		List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
		quickReplies.add(new ChatResponseVO.QuickReply("确认创建", "确认", "primary"));
		return quickReplies;
	}

	@Override
	public ChatResponseVO confirm(ConversationState state) {
		log.info("=== confirm 方法开始执行 ===");
		try {
			// 1. 处理客户（如果是新客户则创建）
			log.info("步骤1: 处理客户");
			Long customerId = handleCustomerCreation(state);
			log.info("客户ID: {}", customerId);

			// 2. 构建出库单保存请求
			com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutSaveReqVO orderReq =
				new com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutSaveReqVO();

			orderReq.setCustomerId(customerId);
			orderReq.setWarehouseId(getDefaultWarehouseId(state));
			orderReq.setOutDate(LocalDate.now());
			orderReq.setSourceType(1);
			orderReq.setHandler((String) state.getData("handler"));
			orderReq.setRemark((String) state.getData("remark"));

			// 3. 构建订单明细
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");

			List<com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemSaveReqVO> itemReqList = new ArrayList<>();

			for (Map<String, Object> item : items) {
				com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemSaveReqVO itemReq =
					new com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemSaveReqVO();

				// SKU ID - 对于销售订单，skuId是必填的
				Object skuIdObj = item.get("skuId");
				if (skuIdObj != null && skuIdObj instanceof Number) {
					itemReq.setSkuId(((Number) skuIdObj).longValue());
				} else {
					log.warn("跳过没有skuId的商品: {}", item.get("name"));
					continue;
				}

				// 数量和单价
				Object quantityObj = item.get("quantity");
				if (quantityObj instanceof Number) {
					itemReq.setQuantity(new BigDecimal(quantityObj.toString()));
				}

				Object priceObj = item.get("price");
				if (priceObj instanceof Number) {
					itemReq.setPrice(new BigDecimal(priceObj.toString()));
				}

				itemReqList.add(itemReq);
			}

			orderReq.setItems(itemReqList);

			// 4. 调用service保存出库单
			Long orderId = salesOrderService.create(orderReq);
			log.info("销售出库单创建成功: orderId={}", orderId);

			// 5. 获取创建的出库单信息
			com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO createdOrder = salesOrderService.get(orderId);
			String orderNo = createdOrder != null ? createdOrder.getOutNo() : "CK" + System.currentTimeMillis();

			// 6. 构建成功响应（传递 orderId）
			return buildSuccessResponse(state, orderNo, orderId, items);

		} catch (Exception e) {
			log.error("创建销售出库单失败", e);
			state.setStatus(ConversationState.SessionStatus.CANCELLED);

			// 构建快捷回复选项：重新创建
			List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
			quickReplies.add(new ChatResponseVO.QuickReply("重新创建", "重新创建销售出库", "primary"));

			// 处理库存不足的错误信息，将 SKU 替换为商品名称
			String errorMessage = e.getMessage();
			if (errorMessage != null && errorMessage.contains("库存不足")) {
				// 尝试从当前商品列表中提取商品名称
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");
				if (items != null && !items.isEmpty()) {
					StringBuilder goodsNames = new StringBuilder();
					for (int i = 0; i < items.size(); i++) {
						Map<String, Object> item = items.get(i);
						if (i > 0) {
							goodsNames.append("、");
						}
						goodsNames.append(item.get("name"));
					}
					errorMessage = goodsNames + "库存不足，无法出库";
				}
			}

			return response(state, "❌ **创建销售出库单失败**\n\n" + errorMessage + "\n\n请点击下方按钮或输入新的指令重新开始。")
					.withQuickReplies(quickReplies)
					.isCompleted(true);
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
	 * 处理客户创建（如果是新客户）
	 */
	private Long handleCustomerCreation(ConversationState state) {
		Object customerIdObj = state.getData("customerId");

		// 如果已有客户ID，直接返回
		if (customerIdObj != null && customerIdObj instanceof Number) {
			Long id = ((Number) customerIdObj).longValue();
			if (id > 0) {
				return id;
			}
		}

		// 如果是新客户，创建客户
		if (Boolean.TRUE.equals(state.getData("isNewCustomer"))) {
			String customerName = (String) state.getData("customerName");
			if (customerName != null && !customerName.isEmpty()) {
				com.ruoyi.wms.domain.vo.customer.WmsCustomerSaveReqVO customerReq =
					new com.ruoyi.wms.domain.vo.customer.WmsCustomerSaveReqVO();

				// 生成客户编码
				String customerCode = "CUS" + System.currentTimeMillis();
				customerReq.setCustomerCode(customerCode);
				customerReq.setCustomerName(customerName);
				customerReq.setStatus("0"); // 正常状态
				customerReq.setRemark("AI助手自动创建");

				Long newCustomerId = customerService.create(customerReq);
				log.info("自动创建客户成功: customerId={}, customerName={}", newCustomerId, customerName);
				return newCustomerId;
			}
		}

		throw new RuntimeException("客户信息不完整，无法创建订单");
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
		sb.append("✅ **销售出库单创建成功！**\n\n");
		sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
		sb.append("📄 出库单号：").append(orderNo).append("\n");
		sb.append("👤 客户：").append(state.getData("customerName")).append("\n");
		// 统一摘要顺序：客户 -> 仓库 -> 出库日期 -> 商品 -> 数量 -> 单价 -> 金额
		String warehouseName = state.getData("warehouseName", "默认仓库");
		sb.append("🏠 仓库：").append(warehouseName).append("\n");
		sb.append("📅 出库日期：").append(LocalDate.now().format(DATE_FORMATTER)).append("\n");

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
		if (Boolean.TRUE.equals(state.getData("isNewCustomer"))) {
			newItems.add("客户：" + state.getData("customerName"));
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
		action.setName("edit_sales_out");
		action.setDescription("查看/编辑出库单");
		action.setArguments(arguments);

		// 添加快捷回复选项：继续创建订单
		List<ChatResponseVO.QuickReply> quickReplies = new ArrayList<>();
		quickReplies.add(new ChatResponseVO.QuickReply("创建下一条出库单", "继续创建销售出库", "primary"));

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
		return response(state, "❌ 已取消创建销售出库单。").isCompleted(true);
	}

	/**
	 * 处理完整订单（智能解析后直接生成预览）
	 * 支持语音/文本一次性输入完整订单信息
	 */
	@SuppressWarnings("unchecked")
	public ChatResponseVO handleCompleteOrder(ConversationState state, ParsedOrderResult parsedResult) {
		log.info("处理完整销售出库: {}", parsedResult.getOriginalMessage());

		// 初始化商品列表
		state.setData("items", new ArrayList<Map<String, Object>>());
		state.setCurrentStep(STEP_CONFIRM_ORDER);

		// 处理客户信息
		if (parsedResult.hasCustomer()) {
			String customerName = parsedResult.getCustomer();
			WmsCustomer customer = findCustomer(customerName);
			if (customer != null) {
				state.setData("customerId", customer.getId());
				state.setData("customerName", customer.getCustomerName());
				state.setData("isNewCustomer", false);
			} else {
				state.setData("customerId", null);
				state.setData("customerName", customerName);
				state.setData("isNewCustomer", true);
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

		// 显示订单确认
		return showOrderConfirm(state);
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


	private ChatResponseVO responseWithCustomerList(ConversationState state, String message) {
		StringBuilder sb = new StringBuilder(message);
		@SuppressWarnings("unchecked")
		List<WmsCustomer> customers = (List<WmsCustomer>) state.getData("availableCustomers");
		if (customers != null && !customers.isEmpty()) {
			sb.append("\n\n常用客户（输入关键字即可）：\n");
			for (int i = 0; i < customers.size(); i++) {
				sb.append(i + 1).append(". ").append(customers.get(i).getCustomerName()).append("\n");
			}
			sb.append(customers.size() + 1).append(". 新建客户\n");
		}
		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(sb.toString())
				.needMoreInfo(true)
				.pendingFields(Arrays.asList("customer"))
				.collectedData(state.getCollectedData())
				.build();
	}

	private ChatResponseVO askGoodsQuantity(ConversationState state, String goodsName) {
		state.setCurrentStep(STEP_GOODS_QUANTITY);

		// 检查商品是否有销售价格
		@SuppressWarnings("unchecked")
		Map<String, Object> currentGoods = (Map<String, Object>) state.getData("currentGoods");
		BigDecimal salePrice = null;
		if (currentGoods != null && currentGoods.get("salePrice") != null) {
			Object priceObj = currentGoods.get("salePrice");
			if (priceObj instanceof BigDecimal) {
				salePrice = (BigDecimal) priceObj;
			} else if (priceObj != null) {
				salePrice = new BigDecimal(priceObj.toString());
			}
		}

		StringBuilder reply = new StringBuilder();
		reply.append("📦 ").append(goodsName).append("\n\n");
		if (salePrice != null) {
			reply.append("💰 参考销售价：¥").append(salePrice).append("\n\n");
		}
		reply.append("请输入**数量**（如：100、50）？");

		return response(state, reply.toString());
	}

	// ==================== 数据查找 ====================

	@SuppressWarnings("unchecked")
	private WmsCustomer pickDefaultCustomer(ConversationState state) {
		// 优先从数据库查 is_default=1 的默认客户
		List<WmsCustomer> defaultCustomers = customerService.list(
				Wrappers.<WmsCustomer>lambdaQuery()
						.eq(WmsCustomer::getIsDefault, 1)
						.eq(WmsCustomer::getStatus, "0")
						.last("LIMIT 1")
		);
		if (defaultCustomers != null && !defaultCustomers.isEmpty()) {
			return defaultCustomers.get(0);
		}

		// 没有默认客户，如果只有1个客户则自动选中
		List<WmsCustomer> allCustomers = customerService.list(
				Wrappers.<WmsCustomer>lambdaQuery()
						.eq(WmsCustomer::getStatus, "0")
		);
		if (allCustomers != null && allCustomers.size() == 1) {
			return allCustomers.get(0);
		}

		// 多个客户且无默认，尝试从 start() 阶段缓存的列表取第一个
		List<WmsCustomer> availableCustomers = (List<WmsCustomer>) state.getData("availableCustomers");
		if (availableCustomers != null && !availableCustomers.isEmpty()) {
			return availableCustomers.get(0);
		}

		return null;
	}

	private WmsCustomer findCustomer(String name) {
		List<WmsCustomer> customers = customerService.list(
				Wrappers.<WmsCustomer>lambdaQuery()
						.like(WmsCustomer::getCustomerName, name)
						.orderByDesc(WmsCustomer::getCreateTime)
						.last("LIMIT 1")
		);
		return customers.isEmpty() ? null : customers.get(0);
	}

	private WmsGoodsSkuRespVO findGoods(String name) {
		log.info("findGoods 被调用, 参数: {}", name);
		try {
			List<WmsGoodsSkuRespVO> skus = goodsSkuService.searchByName(name, 5);
			log.info("findGoods 查询结果数量: {}", skus.size());
			return skus.isEmpty() ? null : skus.get(0);
		} catch (Exception e) {
			log.warn("查找商品失败: {}", name, e);
			return null;
		}
	}

	/**
	 * 查找商品列表
	 */
	private List<WmsGoodsSkuRespVO> findGoodsList(String name) {
		log.info("findGoodsList 被调用, 参数: {}", name);
		try {
			List<WmsGoodsSkuRespVO> skus = goodsSkuService.searchByName(name, 3);
			log.info("findGoodsList 查询结果数量: {}", skus.size());
			return skus;
		} catch (Exception e) {
			log.warn("查找商品列表失败: {}", name, e);
			return new ArrayList<>();
		}
	}

	// ==================== 数据解析 ====================

	private Map<String, String> parseCustomerInfo(String input) {
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

		// 去掉末尾的标点符号（语音识别可能添加）
		input = input.replaceAll("[。.!！?？，,、]+$", "").trim();

		log.info("parseGoodsInput 开始处理, 输入: {}", input);

		// 提取客户信息（销售单专用）
		// 格式1: "出货给XXX" / "卖给XXX" / "销售给XXX" / "发给XXX"
		// 使用非捕获组匹配: 出货给|销货给|出给|销给|卖给|发给
		java.util.regex.Pattern customerPattern1 = java.util.regex.Pattern.compile("(?:[出销]货?|卖|发)给\\s*([^，,。\\d]{2,15})");
		java.util.regex.Matcher customerMatcher1 = customerPattern1.matcher(input);
		if (customerMatcher1.find()) {
			String customer = customerMatcher1.group(1);
			if (customer != null) {
				customer = customer.trim();
				if (!customer.matches("(我|你|他|她|它|你们|我们|他们|客户)")) {
					result.put("customer", customer);
					input = input.replaceFirst("(?:[出销]货?|卖|发)给\\s*" + java.util.regex.Pattern.quote(customer), "").trim();
					log.info("提取到客户(格式1): {}", customer);
				}
			}
		}

		// 格式2: "给XXX发" / "给XXX客户发" (注意: "发" 可以单独出现, 后面可以跟数字)
		if (!result.containsKey("customer")) {
			// 先尝试 "给XXX客户" 格式
			java.util.regex.Pattern customerPattern2a = java.util.regex.Pattern.compile("给\\s*([^，,。\\d]{2,15}?)\\s*客户");
			java.util.regex.Matcher customerMatcher2a = customerPattern2a.matcher(input);
			if (customerMatcher2a.find()) {
				String customer = customerMatcher2a.group(1);
				if (customer != null) {
					customer = customer.trim();
					if (!customer.matches("(我|你|他|她|它|你们|我们|他们)")) {
						result.put("customer", customer);
						input = input.replaceFirst("给\\s*" + java.util.regex.Pattern.quote(customer) + "\\s*客户", "").trim();
						log.info("提取到客户(格式2a-给XXX客户): {}", customer);
					}
				}
			}
		}

		// 格式3: "给XXX出货" / "给XXX卖" / "给XXX发货"
		if (!result.containsKey("customer")) {
			// 修正正则表达式：使用非捕获组包裹选项
			java.util.regex.Pattern customerPattern3 = java.util.regex.Pattern.compile("给\\s*([^，,。\\d]{2,15}?)\\s*(?:[出销]货|发货|卖)");
			java.util.regex.Matcher customerMatcher3 = customerPattern3.matcher(input);
			if (customerMatcher3.find()) {
				String customer = customerMatcher3.group(1);
				if (customer != null) {
					customer = customer.trim();
					if (!customer.matches("(我|你|他|她|它|你们|我们|他们)")) {
						result.put("customer", customer);
						input = input.replaceFirst("给\\s*" + java.util.regex.Pattern.quote(customer) + "\\s*(?:[出销]货?|发货|卖)", "").trim();
						log.info("提取到客户(格式3-给XXX出货/卖/发货): {}", customer);
					}
				}
			}
		}

		// 格式4: "客户XXX" / "客户：XXX" / "客户是XXX"
		if (!result.containsKey("customer")) {
			java.util.regex.Pattern customerPattern4 = java.util.regex.Pattern.compile("客户[：:是为]?\\s*([^，,。\\d]{2,15})");
			java.util.regex.Matcher customerMatcher4 = customerPattern4.matcher(input);
			if (customerMatcher4.find()) {
				String customer = customerMatcher4.group(1);
				if (customer != null) {
					result.put("customer", customer);
					input = input.replaceFirst("客户[：:是为]?\\s*" + java.util.regex.Pattern.quote(customer), "").trim();
					log.info("提取到客户(格式4-客户XXX): {}", customer);
				}
			}
		}

		// 清理 "每X" 格式的价格前缀（如 "每箱80元" 中的 "每箱"）
		input = input.replaceAll("每[箱瓶个件台套条斤kg克]+", "").trim();

		// 提取数量
		Integer quantity = parseQuantity(input);
		if (quantity != null) {
			result.put("quantity", quantity);
			input = input.replaceAll("\\d+\\s*[箱瓶个件台套条斤kg克]+", "").trim();
		}

		// 提取价格
		BigDecimal price = parsePrice(input);
		if (price != null) {
			result.put("price", price);
			input = input.replaceAll("\\d+\\.?\\d*\\s*元?", "").trim();
		}

		// 提取条形码
		java.util.regex.Pattern barcodePattern = java.util.regex.Pattern.compile("\\d{8,20}");
		java.util.regex.Matcher barcodeMatcher = barcodePattern.matcher(input);
		if (barcodeMatcher.find()) {
			result.put("barcode", barcodeMatcher.group());
			input = input.replace(barcodeMatcher.group(), "").trim();
		}

		// 提取单位
		java.util.regex.Pattern unitPattern = java.util.regex.Pattern.compile("(箱|瓶|个|包|袋|盒|台|套|条|斤|kg|克|L|ml|件)");
		java.util.regex.Matcher unitMatcher = unitPattern.matcher(input);
		if (unitMatcher.find()) {
			result.put("unit", unitMatcher.group());
			input = input.replace(unitMatcher.group(), "").trim();
		}

		// 提取规格（剩余的主要文本，通常是规格）
		input = input.replaceAll("^(商品[：:]?|名称[：:]?|货品[：:]?|物品[：:]?)\\s*", "").trim();
		input = input.replaceAll("[。.!！?？，,、]+$", "").trim();
		
		// 清理常见的销售相关词汇
		input = input.replaceAll("(售价|单价[是为]?|价格[是为]?)", "").trim();
		input = input.replaceAll("^发\\s*", "").trim();
		input = input.replaceAll("^(?:出货|销货|发货|销售|售出|卖出|卖|发|出|销)\\s*", "").trim();
		input = input.replaceAll("[，,。！!？?、：:]+", " ").trim();
		
		if (!input.isEmpty() && !input.matches("\\d+")) {
			result.put("spec", input);
		}

		log.info("parseGoodsInput 最终结果: customer={}, name={}, spec={}, unit={}, price={}",
				result.get("customer"), result.get("name"), result.get("spec"), result.get("unit"), result.get("price"));
		result.put("name", input);

		// 强校验商品名称必须存在
		String goodsName = (String) result.get("name");
		if (goodsName == null || goodsName.isEmpty()) {
			result.put("error", "您的描述中缺少商品信息。\n\n请重新提供完整信息，例如：\n**给京东客户发 200 个无线鼠标，单价 60 元**");
			return result;
		}


		// 强校验：一句话销售出库必须包含客户信息
		String customer = (String) result.get("customer");
		if (customer == null || customer.isEmpty()) {
			result.put("error", "您的描述中缺少客户信息。\n\n请重新提供完整信息，例如：\n**给京东客户发 200 个无线鼠标，单价 60 元**");
			return result;
		}

		// 强校验：一句话销售出库必须同时包含数量和价格
		quantity = (Integer) result.get("quantity");
		price = (BigDecimal) result.get("price");

		
		if (quantity == null && price == null) {
			// 数量和价格都缺少
			result.put("error", "您的描述中缺少商品数量和价格信息。\n\n请重新提供完整信息，例如：\n**给京东客户发200个无线鼠标，单价60元**");
		} else if (quantity == null) {
			// 只缺少数量
			result.put("error", "您的描述中缺少商品数量信息。\n\n请重新提供完整信息，例如：\n**给京东客户发200个无线鼠标，单价60元**");
		} else if (price == null) {
			// 只缺少价格
			result.put("error", "您的描述中缺少商品价格信息。\n\n请重新提供完整信息，例如：\n**给京东客户发200个无线鼠标，单价60元**");
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
		} else if (sku.getSalePrice() != null) {
			map.put("price", sku.getSalePrice());
		}

		return map;
	}

	private ChatResponseVO reviewItems(ConversationState state) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> items = (List<Map<String, Object>>) state.getData("items");

		if (items.isEmpty()) {
			state.setCurrentStep(STEP_GOODS);
			return response(state, "还没有添加任何商品。请告诉我：**要销售什么商品？**");
		}
		return askMoreGoods(state);
	}

	// ==================== 判断方法 ====================

	private boolean isConfirm(String message) {
		return "确认".equals(message) || "yes".equalsIgnoreCase(message)
				|| "好的".equals(message) || "行".equals(message);
	}

	private boolean isYes(String message) {
		return "是".equals(message) || "yes".equalsIgnoreCase(message)
				|| "是的".equals(message) || "对".equals(message);
	}

	private boolean isCancel(String message) {
		return "取消".equals(message) || "不".equals(message) || "no".equalsIgnoreCase(message)
				|| "否".equals(message);
	}

	/**
	 * 清理语音识别可能添加的标点符号
	 */
	private String cleanPunctuation(String message) {
		if (message == null) return "";
		return message.replaceAll("[。.!！?？，,、]+$", "").trim();
	}

	/**
	 * 验证商品名称是否有效
	 * 至少2个字符，不能全是特殊符号
	 */
	private boolean isValidGoodsName(String name) {
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		if (isLikelyFilePath(name)) {
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

	private boolean isLikelyFilePath(String text) {
		if (text == null) {
			return false;
		}
		String normalized = text.trim().toLowerCase(Locale.ROOT);
		if (normalized.isEmpty()) {
			return false;
		}
		boolean hasDrivePrefix = normalized.matches("^[a-z]:\\\\.*");
		boolean hasSlashPath = normalized.contains("\\\\") || normalized.contains("/");
		boolean hasImageSuffix = normalized.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$");
		boolean hasTempMarker = normalized.contains("rwtemp") || normalized.contains("xwechat_files") || normalized.contains("temp");
		return hasImageSuffix && (hasDrivePrefix || hasSlashPath || hasTempMarker);
	}

	/**
	 * 生成客户编码
	 */
	private String generateCustomerCode() {
		String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		String prefix = "CT" + datePart;

		List<WmsCustomer> customers = customerService.list(
			Wrappers.<WmsCustomer>lambdaQuery()
				.likeRight(WmsCustomer::getCustomerCode, prefix)
				.orderByDesc(WmsCustomer::getCustomerCode)
				.last("LIMIT 1")
		);

		int nextSeq = 1;
		if (customers != null && !customers.isEmpty() && customers.get(0).getCustomerCode() != null
				&& customers.get(0).getCustomerCode().length() >= prefix.length() + 4) {
			String suffix = customers.get(0).getCustomerCode().substring(prefix.length());
			try {
				nextSeq = Integer.parseInt(suffix) + 1;
			} catch (NumberFormatException ignore) {
				nextSeq = 1;
			}
		}
		return prefix + String.format("%04d", nextSeq);
	}

	/**
	 * 跳转到移动端修改页面
	 */
	@SuppressWarnings("unchecked")
	private ChatResponseVO navigateToModifyPage(ConversationState state) {
		log.info("跳转到移动端修改页面");

		// 收集当前订单数据
		Map<String, Object> orderData = new HashMap<>();
		orderData.put("customerId", state.getData("customerId"));
		orderData.put("customerName", state.getData("customerName"));
		orderData.put("warehouseId", state.getData("warehouseId"));
		orderData.put("warehouseName", state.getData("warehouseName"));
		orderData.put("outDate", state.getData("outDate"));
		orderData.put("remark", state.getData("remark"));
		orderData.put("items", state.getData("items"));
		orderData.put("conversationId", state.getConversationId());
		orderData.put("userId", state.getUserId());

		// 生成临时数据 key（使用 conversationId）
		String tempDataKey = "temp_sales_order_" + state.getConversationId();

		// 将数据保存到 Redis（过期时间 30 分钟）
		try {
			com.ruoyi.common.core.redis.RedisCache redisCache = com.ruoyi.common.utils.spring.SpringUtils.getBean(com.ruoyi.common.core.redis.RedisCache.class);
			redisCache.setCacheObject(tempDataKey, orderData, 30, java.util.concurrent.TimeUnit.MINUTES);
			log.info("临时订单数据已保存到 Redis: {}", tempDataKey);
		} catch (Exception e) {
			log.error("保存临时订单数据失败：{}", e.getMessage());
		}

		// 返回跳转指令
		ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
		action.setName("navigateTo");
		action.setDescription("跳转到销售出库修改页面");
		Map<String, Object> args = new HashMap<>();
		args.put("url", "/subpackages/sales/out-add");
		args.put("fromChat", true);
		args.put("tempDataKey", tempDataKey);
		action.setArguments(args);

		return response(state, "好的，已跳转到销售出库修改页面。\n\n- 当前订单数据已自动带入\n- 修改并保存后，数据将同步回聊天界面")
				.suggestedAction(action);
	}
}
