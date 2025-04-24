package org.apache.camel.demo.behavior;

import java.time.Duration;
import javax.sql.DataSource;

import org.apache.camel.demo.model.Product;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;

import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.citrusframework.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;

public class WaitForProductCreated implements TestBehavior {

    private final Product product;
    private final DataSource dataSource;

    public WaitForProductCreated(Product product, DataSource dataSource) {
        this.product = product;
        this.dataSource = dataSource;
    }

    @Override
    public void apply(TestActionRunner t) {
        t.run(repeatOnError()
            .condition((i, context) -> i > 25)
            .autoSleep(Duration.ofMillis(1000L))
            .actions(sql().dataSource(dataSource)
                    .query()
                    .statement("select count(id) as found from product where product.name='%s'"
                            .formatted(product.getName()))
                    .validate("found", "1"))
        );
    }
}
