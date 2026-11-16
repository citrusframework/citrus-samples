package org.citrusframework.samples.connectors.kubernetes;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class TodoListYamlIT extends AbstractKubernetesIT {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/connectors/kubernetes/deployment-state.citrus.it.yaml" })
    public void testDeploymentState() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/connectors/kubernetes/todo-service.citrus.it.yaml" })
    public void testTodoService() {
    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        super.run(callBack, testResult);
        if (testResult.getStatus() == ITestResult.STARTED) {
            // No Kubernetes environment available, base class skipped execution
            testResult.setStatus(ITestResult.SKIP);
        }
    }
}
