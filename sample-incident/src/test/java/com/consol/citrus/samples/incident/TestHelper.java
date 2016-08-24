/*
 * Copyright 2006-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.incident;

import java.util.Calendar;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public abstract class TestHelper {

    /**
     * Prevent instantiation.
     */
    private TestHelper() {
    }

    /**
     * Provides default schedule time which is current timestamp +1h with
     * normalized minutes and seconds.
     *
     * @return
     */
    public static Calendar getDefaultScheduleTime() {
        Calendar scheduled = Calendar.getInstance();
        scheduled.add(Calendar.HOUR, 1);

        scheduled.set(Calendar.MINUTE, 0);
        scheduled.set(Calendar.SECOND, 0);
        scheduled.set(Calendar.MILLISECOND, 0);

        return scheduled;
    }
}
