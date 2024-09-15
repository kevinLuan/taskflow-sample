package cn.taskflow.sample.workflow;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.client.core.FeiLiuWorkflow;
import cn.feiliu.taskflow.common.run.ExecutingWorkflow;

import static cn.feiliu.taskflow.expression.Expr.*;

import cn.feiliu.taskflow.expression.Pair;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.Dynamic;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.For;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.WorkTask;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-07
 */
@Service
public class SimpleDynamicTaskWorkflow implements IWorkflowService {
    @Autowired
    private ApiClient apiClient;
    @Getter
    private String name = "simple-dynamic-workflow";
    @Getter
    private int version = 2;

    @Override
    public boolean register() {
        FeiLiuWorkflow<Map<String, Object>> workflow = apiClient.newWorkflowBuilder(name, version)
                // 获取订单列表任务
                .add(new WorkTask("getOrders", "getOrdersRef"))
                // 循环遍历任务
                .add(new For("orderRef", task("getOrdersRef").output.get("result"))
                        .loopOver(
                                new WorkTask("expressDelivery", "expressDeliveryRef")
                                        .input(Pair.of("order").fromTaskOutput("orderRef", "element")),
                                new Dynamic("dynamicExpressDeliveryRef", task("expressDeliveryRef").output.get("expressType"))//
                                        .input(Pair.of("delivery").fromTaskOutput("expressDeliveryRef"))
                        )
                ).build();
        return workflow.registerWorkflow(true, true);
    }

    @Override
    public CompletableFuture<ExecutingWorkflow> run() {
        Map<String, Object> dataMap = new HashMap<>();
        return apiClient.getWorkflowExecutor().executeWorkflow(name, version, dataMap);
    }
}
