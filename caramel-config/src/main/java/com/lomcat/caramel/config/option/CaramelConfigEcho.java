/*
 * Copyright 2018-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lomcat.caramel.config.option;

/**
 * <h3>Caramel 加载的配置文件内容打印</h3>
 *
 * @author Kweny
 * @since 0.0.1
 */
public class CaramelConfigEcho {

    public static final String LEVEL_NONE = "none";
    public static final String LEVEL_SUMMARY = "summary";
    public static final String LEVEL_DETAIL = "detail";

    /**
     * 打印内容的细节度：none-不打印；summary-打印概要；detail-打印详情
     */
    private String level;
    /**
     * 敏感数据打印时是否脱敏
     */
    private boolean masking;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean isMasking() {
        return masking;
    }

    public void setMasking(boolean masking) {
        this.masking = masking;
    }

    public void echo() {

    }
}