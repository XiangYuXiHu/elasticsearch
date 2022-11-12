package com.smile.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.*;

/**
 * @Description
 * @ClassName BeanUtil
 * @Author smile
 * @date 2022.11.12 15:59
 */
public class BeanUtil {

    public static String[] getNoValuePropertyNames(Object source) {
        Assert.notNull(source, "source对象不能为空");
        BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] descriptors = beanWrapper.getPropertyDescriptors();

        Set<String> noValuePropertySet = new HashSet<>();
        Arrays.stream(descriptors).forEach(pd -> {
            Object propertyValue = beanWrapper.getPropertyValue(pd.getName());
            if (StringUtils.isEmpty(propertyValue)) {
                noValuePropertySet.add(pd.getName());
            } else {
                if (Iterable.class.isAssignableFrom(propertyValue.getClass())) {
                    Iterable iterable = (Iterable) propertyValue;
                    Iterator iterator = iterable.iterator();
                    if (!iterator.hasNext()) {
                        noValuePropertySet.add(pd.getName());
                    }
                }
                if (Map.class.isAssignableFrom(propertyValue.getClass())) {
                    Map map = (Map) propertyValue;
                    if (map.isEmpty()) {
                        noValuePropertySet.add(pd.getName());
                    }
                }
            }
        });
        String[] result = new String[noValuePropertySet.size()];
        return noValuePropertySet.toArray(result);
    }
}
