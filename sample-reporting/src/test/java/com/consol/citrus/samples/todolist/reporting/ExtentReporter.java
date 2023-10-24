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

package com.consol.citrus.samples.todolist.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.report.AbstractTestReporter;
import org.citrusframework.report.TestListener;
import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestResults;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Christoph Deppisch
 */
public class ExtentReporter extends AbstractTestReporter implements TestReporter, TestListener, InitializingBean {

    private ExtentReports extentReports;

    @Override
    public void onTestSuccess(final TestCase test) {
        final ExtentTest extentTest = extentReports.createTest(test.getName());
        extentTest.pass(getTestDetails(test.getMetaInfo()));
    }

    @Override
    public void onTestSkipped(final TestCase test) {
        final ExtentTest extentTest = extentReports.createTest(test.getName());
        extentTest.skip(getTestDetails(test.getMetaInfo()));
    }

    @Override
    public void onTestFailure(final TestCase test, final Throwable cause) {
        final ExtentTest extentTest = extentReports.createTest(test.getName());
        extentTest.fail(cause);
    }

    @Override
    public void onTestStart(TestCase test) {
        // do nothing
    }

    @Override
    public void onTestFinish(TestCase test) {
        // do nothing
    }

    @Override
    public void generate(TestResults results) {
        extentReports.flush();
    }

    @Override
    public void afterPropertiesSet() {
        initializeExtentReports();
    }

    /**
     * Initialize reports.
     */
    private void initializeExtentReports() {
        final ExtentHtmlReporter extentHtmlReporter = new ExtentHtmlReporter("target/citrus-reports/extent-reports.html");
        extentHtmlReporter.config().setDocumentTitle("ExtentReports - Created by Citrus TestListener");
        extentHtmlReporter.config().setReportName("ExtentReports - Created by Citrus TestListener");
        extentHtmlReporter.config().setTheme(Theme.STANDARD);

        extentReports = new ExtentReports();
        extentReports.attachReporter(extentHtmlReporter);
        extentReports.setReportUsesManualConfiguration(true);
    }

    /**
     * Get test details from meta info.
     * @param metaInfo
     * @return
     */
    private String getTestDetails(final TestCaseMetaInfo metaInfo) {
        return String.format("details: author:%s, creationDate:%s, status:%s", metaInfo.getAuthor(), metaInfo.getCreationDate(), metaInfo.getStatus());
    }


}
