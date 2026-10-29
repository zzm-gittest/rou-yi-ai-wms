package com.ruoyi.common.utils.bean;

import java.util.List;
import java.util.stream.Collectors;

/**
 * VO 转换工具类
 *
 * @author ruoyi
 */
public class VoUtils {

    /**
     * 对象列表转换为 VO 列表
     *
     * @param sources      源对象列表
     * @param targetClass  目标 VO 类
     * @param <S>          源类型
     * @param <T>          目标类型
     * @return VO 列表
     */
    public static <S, T> List<T> convertList(List<S> sources, Class<T> targetClass) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        return sources.stream()
                .map(source -> convert(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * 对象转换为 VO
     *
     * @param source       源对象
     * @param targetClass  目标 VO 类
     * @param <S>          源类型
     * @param <T>          目标类型
     * @return VO 对象
     */
    public static <S, T> T convert(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyBeanProp(target, source);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("VO 转换失败", e);
        }
    }

    /**
     * VO 转换为实体对象
     *
     * @param vo           VO 对象
     * @param targetClass  目标实体类
     * @param <V>          VO 类型
     * @param <T>          目标类型
     * @return 实体对象
     */
    public static <V, T> T toEntity(V vo, Class<T> targetClass) {
        return convert(vo, targetClass);
    }
}
