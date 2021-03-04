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

package com.consol.citrus.samples.kubernetes;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.consol.citrus.kubernetes.actions.KubernetesExecuteAction.Builder.kubernetes;

/**
 * @author Christoph Deppisch
 */
public class ListKubernetesResourcesIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    @Test
    @CitrusTest
    public void testListNodes() {
        $(kubernetes()
            .client(k8sClient)
            .nodes()
            .list()
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test
    @CitrusTest
    public void testListNamespaces() {
        $(kubernetes()
            .client(k8sClient)
            .namespaces()
            .list()
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test
    @CitrusTest
    public void testListEndpoints() {
        $(kubernetes()
            .client(k8sClient)
            .endpoints()
            .list()
            .namespace("default")
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test
    @CitrusTest
    public void testListServices() {
        $(kubernetes()
            .client(k8sClient)
            .services()
            .list()
            .namespace("default")
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test
    @CitrusTest
    public void testListPods() {
        $(kubernetes()
            .client(k8sClient)
            .pods()
            .list()
            .namespace("default")
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test
    @CitrusTest
    public void testListReplicationControllers() {
        $(kubernetes()
            .client(k8sClient)
            .replicationControllers()
            .list()
            .namespace("default")
            .validate((result, context) -> {
                Assert.assertTrue(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }
}
