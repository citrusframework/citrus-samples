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

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ListKubernetesResourcesIT extends AbstractKubernetesIT implements TestActionSupport {

    @Autowired
    private KubernetesClient k8sClient;

    @Test(enabled = false)
    @CitrusTest
    public void testListNodes() {
        $(kubernetes()
            .client(k8sClient)
            .execute()
            .nodes()
            .list()
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test(enabled = false)
    @CitrusTest
    public void testListNamespaces() {
        $(kubernetes()
            .client(k8sClient)
            .execute()
            .namespaces()
            .list()
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test(enabled = false)
    @CitrusTest
    public void testListEndpoints() {
        $(kubernetes()
            .client(k8sClient)
            .execute()
            .endpoints()
            .list()
            .namespace("default")
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test(enabled = false)
    @CitrusTest
    public void testListServices() {
        $(kubernetes()
            .client(k8sClient)
            .execute()
            .services()
            .list()
            .namespace("default")
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test(enabled = false)
    @CitrusTest
    public void testListPods() {
        $(kubernetes()
            .client(k8sClient)
            .execute()
            .pods()
            .list()
            .namespace("default")
            .validate((result, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }

    @Test(enabled = false)
    @CitrusTest
    public void testListReplicationControllers() {
        $(kubernetes()
            .client(k8sClient)
            .execute()
            .replicationControllers()
            .list()
            .namespace("default")
            .validate((result, context) -> {
                Assert.assertTrue(CollectionUtils.isEmpty(result.getResult().getItems()));
            }));
    }
}
