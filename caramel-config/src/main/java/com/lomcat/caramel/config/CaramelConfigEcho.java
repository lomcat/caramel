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

package com.lomcat.caramel.config;

import com.lomcat.caramel.assist.CaramelAide;
import com.lomcat.caramel.assist.CaramelLogger;

/**
 * <h3>Caramel 加载的配置文件内容打印</h3>
 *
 * @author Kweny
 * @since 0.0.1
 */
public class CaramelConfigEcho {

    private static final CaramelLogger logger = CaramelLogger.getLogger(CaramelConfigEcho.class);

    public static final String WAVE_SUMMARY = "summary";
    public static final String WAVE_TRACK = "track";
    public static final String WAVE_CONTENT = "content";

    /**
     * 打印内容的细节度：none-不打印；track-打印加载轨迹；detail-打印配置内容
     */
    private String waves;
    /**
     * 敏感数据打印时是否脱敏
     */
    private boolean masking;// TODO-Kweny 需要配置项支持特殊标记的加密/解密处理

    private boolean summaryEnabled;
    private boolean trackEnabled;
    private boolean contentEnabled;

    public boolean isEchoEnabled() {
        return summaryEnabled || trackEnabled || contentEnabled;
    }

    public boolean isSummaryEnabled() {
        return summaryEnabled || trackEnabled;
    }

    public boolean isTrackEnabled() {
        return trackEnabled;
    }

    public boolean isContentEnabled() {
        return contentEnabled;
    }

    public String getWaves() {
        return waves;
    }

    public void setWaves(String waves) {
        this.waves = waves;
        resolveWaves();
    }

    public boolean isMasking() {
        return masking;
    }

    public void setMasking(boolean masking) {
        this.masking = masking;
    }

    public void echo(String text) {
        logger.info(text);
    }

    private void resolveWaves() {
        if (CaramelAide.isBlank(this.waves)) {
            return;
        }
        String[] waveSections = this.waves.split(",");
        for (String section : waveSections) {
            this.summaryEnabled |= CaramelAide.equalsIgnoreCaseAfterTrim(section, WAVE_SUMMARY);
            this.trackEnabled |= CaramelAide.equalsIgnoreCaseAfterTrim(section, WAVE_TRACK);
            this.contentEnabled |= CaramelAide.equalsIgnoreCaseAfterTrim(section, WAVE_CONTENT);
        }
    }
}