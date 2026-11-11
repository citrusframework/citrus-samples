package com.consol.citrus.samples.kubernetes;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class TodoListXmlIT extends AbstractKubernetesIT {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/kubernetes/deployment-state.citrus.it.xml" })
    public void testDeploymentState() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/kubernetes/todo-service.citrus.it.xml" })
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
