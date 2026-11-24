package com.ruoyi.wms.service.ai.conversation.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.wms.domain.vo.ai.chat.ChatRequestVO;
import com.ruoyi.wms.domain.vo.ai.chat.ChatResponseVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockPageReqVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockRespVO;
import com.ruoyi.wms.service.WmsStockService;
import com.ruoyi.wms.service.ai.conversation.ConversationHandler;
import com.ruoyi.wms.service.ai.conversation.ConversationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 库存查询对话处理器
 *
 * @author ruoyi
 */
@Component
public class StockQueryConversationHandler implements ConversationHandler {

	private static final Logger log = LoggerFactory.getLogger(StockQueryConversationHandler.class);

	@Autowired
	private WmsStockService stockService;

	private static final int MAX_RESULTS = 5;

	@Override
	public String getOperationType() {
		return "query_stock";
	}

	@Override
	public ChatResponseVO start(ConversationState state) {
		return response(state, "🔍 **库存查询**\n\n" +
				"请告诉我您要查询的商品名称，例如：\n" +
				"- \"iPhone\"\n" +
				"- \"苹果\"\n" +
				"- \"显示库存预警\"\n" +
				"- \"全部库存\"");
	}

	@Override
	public ChatResponseVO handle(ConversationState state, ChatRequestVO request) {
		String message = request.getMessage().trim();
		log.info("库存查询 - 输入: {}", message);

		// 清理输入，去掉末尾的标点符号
		message = message.replaceAll("[。.!！?？，,、]+$", "").trim();

		// 检查是否要查询预警库存
		if (message.contains("预警") || message.contains("不足") || message.contains("低库存")) {
			return queryLowStock(state);
		}

		// 检查是否查询全部
		if (message.contains("全部") || message.contains("所有") || message.isEmpty()) {
			return queryAllStock(state);
		}

		// 否则按商品名称查询
		return queryByGoodsName(state, message);
	}

	/**
	 * 查询预警库存
	 */
	private ChatResponseVO queryLowStock(ConversationState state) {
		WmsStockPageReqVO reqVO = new WmsStockPageReqVO();
		reqVO.setLowStock(true);
		reqVO.setPageNo(1);
		reqVO.setPageSize(MAX_RESULTS);

		IPage<WmsStockRespVO> page = stockService.page(reqVO);
		List<WmsStockRespVO> list = page.getRecords();

		return buildStockResponse(state, list, "🔍 **库存预警查询结果**（库存<10）", true);
	}

	/**
	 * 查询全部库存
	 */
	private ChatResponseVO queryAllStock(ConversationState state) {
		WmsStockPageReqVO reqVO = new WmsStockPageReqVO();
		reqVO.setPageNo(1);
		reqVO.setPageSize(MAX_RESULTS);

		IPage<WmsStockRespVO> page = stockService.page(reqVO);
		List<WmsStockRespVO> list = page.getRecords();

		return buildStockResponse(state, list, "🔍 **全部库存查询结果**", true);
	}

	/**
	 * 按商品名称查询
	 */
	private ChatResponseVO queryByGoodsName(ConversationState state, String goodsName) {
		WmsStockPageReqVO reqVO = new WmsStockPageReqVO();
		// 使用keyword参数进行综合搜索（商品名称、SKU名称、规格）
		reqVO.setKeyword(goodsName);
		reqVO.setPageNo(1);
		reqVO.setPageSize(MAX_RESULTS);

		IPage<WmsStockRespVO> page = stockService.page(reqVO);
		List<WmsStockRespVO> list = page.getRecords();

		// 保存查询关键词，用于跳转时传递参数
		state.setData("searchKeyword", goodsName);

		return buildStockResponse(state, list, "🔍 **库存查询结果**：" + goodsName, false);
	}

	/**
	 * 构建库存查询响应
	 */
	private ChatResponseVO buildStockResponse(ConversationState state, List<WmsStockRespVO> list, String title, boolean isAll) {
		StringBuilder sb = new StringBuilder();
		sb.append(title).append("\n");
		sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

		if (list.isEmpty()) {
			sb.append("\n📭 没有找到相关库存信息");
		} else {
			for (int i = 0; i < list.size(); i++) {
				WmsStockRespVO stock = list.get(i);
				sb.append("\n").append(i + 1).append(". **").append(stock.getSkuName()).append("**");

				if (stock.getSpecInfo() != null && !stock.getSpecInfo().isEmpty()) {
					sb.append(" (").append(stock.getSpecInfo()).append(")");
				}

				sb.append("\n   仓库：").append(stock.getWarehouseName());
				sb.append("\n   库存：").append(stock.getQuantity());

				if (stock.getQuantity().compareTo(new java.math.BigDecimal("10")) < 0) {
					sb.append(" ⚠️ 库存不足");
				}

				if (i < list.size() - 1) {
					sb.append("\n");
				}
			}

			// 添加查看更多按钮
			sb.append("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
			sb.append("💡 以上显示前").append(list.size()).append("条结果");
		}

		state.setStatus(ConversationState.SessionStatus.COMPLETED);

		// 添加跳转到库存查询页面的操作按钮
		Map<String, Object> arguments = new HashMap<>();
		// 传递查询关键词
		String searchKeyword = state.getData("searchKeyword", "");
		if (searchKeyword != null && !searchKeyword.isEmpty()) {
			arguments.put("keyword", searchKeyword);
		}

		ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
		action.setName("view_stock_list");
		action.setDescription("查看完整库存");
		action.setArguments(arguments);

		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(sb.toString())
				.suggestedAction(action)
				.collectedData(state.getCollectedData())
				.isCompleted(true)
				.build();
	}

	@Override
	public ChatResponseVO confirm(ConversationState state) {
		return cancel(state);
	}

	@Override
	public ChatResponseVO cancel(ConversationState state) {
		state.setStatus(ConversationState.SessionStatus.CANCELLED);
		return response(state, "❌ 已取消查询。").isCompleted(true);
	}

	@Override
	public String getStepPrompt(ConversationState state) {
		return null;
	}

	private ChatResponseVO response(ConversationState state, String reply) {
		return ChatResponseVO.builder()
				.conversationId(state.getConversationId())
				.operationType(getOperationType())
				.reply(reply)
				.needMoreInfo(true)
				.collectedData(state.getCollectedData())
				.build();
	}
}
