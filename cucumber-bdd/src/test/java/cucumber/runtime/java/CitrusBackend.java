/*
 * Copyright 2006-2016 the original author or authors.
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

package cucumber.runtime.java;

import com.consol.citrus.Citrus;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.*;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

/**
 * @author Christoph Deppisch
 */
public class CitrusBackend implements Backend {

    /** Citrus instance used by all scenarios */
    public static Citrus citrus = Citrus.newInstance();

    /**
     * Constructor using resource loader.
     * @param resourceLoader
     */
    public CitrusBackend(ResourceLoader resourceLoader) {
    }

    /**
     * Constructor using object factory.
     * @param objectFactory
     */
    public CitrusBackend(ObjectFactory objectFactory) {
    }

    /**
     * Constructor using object factory and class finder.
     * @param objectFactory
     * @param classFinder
     */
    public CitrusBackend(ObjectFactory objectFactory, ClassFinder classFinder) {
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        citrus.beforeSuite("cucumber-suite");
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
    }

    @Override
    public void buildWorld() {
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
        return "";
    }
}
