/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.samples.docker;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.citrusframework.TestActionSupport;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.annotations.BeforeSuite;

/**
 * @author Christoph Deppisch
 */
public class AbstractDockerIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AbstractDockerIT.class);

    @Autowired
    private DockerClient dockerClient;

    /** Docker connection state, checks connectivity only once per test run */
    private static boolean connected = false;

    @BeforeSuite(alwaysRun = true)
    public void checkDockerEnvironment() {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> {
                dockerClient.getEndpointConfiguration().getDockerClient().pingCmd().exec();
                return true;
            });

            future.get(5000, TimeUnit.MILLISECONDS);
            connected = true;
        } catch (Exception e) {
            log.warn("Skipping Docker test execution as no proper Docker environment is available on host system!", e);
        }
    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        if (connected) {
            super.run(callBack, testResult);
        }
    }
}
