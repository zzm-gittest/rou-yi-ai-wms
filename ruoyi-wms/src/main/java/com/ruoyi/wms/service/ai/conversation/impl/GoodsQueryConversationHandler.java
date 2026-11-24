package com.ruoyi.wms.service.ai.conversation.impl;

import com.ruoyi.wms.domain.vo.ai.chat.ChatRequestVO;
import com.ruoyi.wms.domain.vo.ai.chat.ChatResponseVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.ai.conversation.ConversationHandler;
import com.ruoyi.wms.service.ai.conversation.ConversationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 商品查询对话处理器
 *
 * @author ruoyi
 */
@Component
public class GoodsQueryConversationHandler implements ConversationHandler {

	private static final Logger log = LoggerFactory.getLogger(GoodsQueryConversationHandler.class);

	@Autowired
	private WmsGoodsSkuService goodsSkuService;

	private static final int MAX_RESULTS = 5;

	@Override
	public String getOperationType() {
		return "query_goods";
	}

	@Override
	public ChatResponseVO start(ConversationState state) {
		return response(state, "🔍 **商品查询**\n\n" +
				"请告诉我您要查询的商品名称，例如：\n" +
				"- \"iPhone\"\n" +
				"- \"苹果\"\n" +
				"- \"饮料\"\n" +
				"- \"显示全部商品\"");
	}

	@Override
	public ChatResponseVO handle(ConversationState state, ChatRequestVO request) {
		String message = request.getMessage().trim();
		log.info("商品查询 - 输入: {}", message);

		// 检查是否查询全部
		if (message.contains("全部") || message.contains("所有") || message.isEmpty()) {
			return queryAllGoods(state);
		}

		// 从语音输入中提取商品名称
		String goodsName = extractGoodsName(message);
		log.info("提取的商品名称: {}", goodsName);

		// 按商品名称查询
		return queryByGoodsName(state, goodsName);
	}

	/**
	 * 从语音/文本输入中提取商品名称
	 * 支持多种表达方式：
	 * - "查询冰红茶"
	 * - "有没有冰红茶"
	 * - "找一下冰红茶"
	 * - "查一下有没有冰红茶的商品"
	 * - "商品：冰红茶"
	 * - "冰红茶"
	 */
	private String extractGoodsName(String message) {
		// 移除常见的查询词
		String goodsName = message
				.replaceAll("(查询|查一下|查|找一下|找|看看|有没有|是否有|搜索|检索)", "")
				.replaceAll("(的商品|商品|产品|物品)", "")
				.trim();

		// 移除开头和结尾的标点符号（中文冒号、英文冒号、顿号、逗号、句号等）
		goodsName = goodsName.replaceAll("^[\\s：:、,，。.!！?？]+", "");
		goodsName = goodsName.replaceAll("[\\s：:、,，。.!！?？]+$", "");

		// 如果处理后为空，返回原始消息
		return goodsName.isEmpty() ? message : goodsName;
	}

	/**
	 * 查询全部商品
	 */
	private ChatResponseVO queryAllGoods(ConversationState state) {
		// 使用分页查询获取所有商品
		com.baomidou.mybatisplus.core.metadata.IPage<WmsGoodsSkuRespVO> page =
			goodsSkuService.page(new com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuPageReqVO());
		List<WmsGoodsSkuRespVO> list = page.getRecords();

		// 限制返回数量
		if (list.size() > MAX_RESULTS) {
			list = list.subList(0, MAX_RESULTS);
		}

		return buildGoodsResponse(state, list, "🔍 **全部商品查询结果**");
	}

	/**
	 * 按商品名称查询
	 */
	private ChatResponseVO queryByGoodsName(ConversationState state, String goodsName) {
		List<WmsGoodsSkuRespVO> list = goodsSkuService.searchByName(goodsName, MAX_RESULTS);
		// 保存查询关键词，用于跳转时传递参数
		state.setData("searchKeyword", goodsName);
		return buildGoodsResponse(state, list, "🔍 **商品查询结果**：" + goodsName);
	}

	/**
	 * 构建商品查询响应
	 */
	private ChatResponseVO buildGoodsResponse(ConversationState state, List<WmsGoodsSkuRespVO> list, String title) {
		StringBuilder sb = new StringBuilder();
		sb.append(title).append("\n");
		sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

		if (list.isEmpty()) {
			sb.append("\n📭 没有找到相关商品信息");
		} else {
			for (int i = 0; i < list.size(); i++) {
				WmsGoodsSkuRespVO sku = list.get(i);
				sb.append("\n").append(i + 1).append(". **").append(sku.getSkuName()).append("**");

				// 规格信息
				if (sku.getSpecInfo() != null && !sku.getSpecInfo().isEmpty()) {
					sb.append(" (").append(sku.getSpecInfo()).append(")");
				}

				// 条形码
				if (sku.getBarcode() != null && !sku.getBarcode().isEmpty()) {
					sb.append("\n   条码：").append(sku.getBarcode());
				}

				// 价格信息
				if (sku.getPurchasePrice() != null || sku.getSalePrice() != null) {
					sb.append("\n   价格：");
					if (sku.getPurchasePrice() != null) {
						sb.append("进¥").append(sku.getPurchasePrice());
					}
					if (sku.getSalePrice() != null) {
						if (sku.getPurchasePrice() != null) {
							sb.append(" | ");
						}
						sb.append("售¥").append(sku.getSalePrice());
					}
				}

				if (i < list.size() - 1) {
					sb.append("\n");
				}
			}

			sb.append("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
			sb.append("💡 以上显示前").append(list.size()).append("条结果");
		}

		state.setStatus(ConversationState.SessionStatus.COMPLETED);

		// 添加跳转到商品列表页面的操作按钮
		Map<String, Object> arguments = new HashMap<>();
		// 传递查询关键词
		String searchKeyword = state.getData("searchKeyword", "");
		if (searchKeyword != null && !searchKeyword.isEmpty()) {
			arguments.put("keyword", searchKeyword);
		}

		ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
		action.setName("view_goods_list");
		action.setDescription("查看完整商品");
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
