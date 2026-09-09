package dio.budgeting.application;

import dio.budgeting.application.output.TotalSpentOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class GetTotalSpentByCategoryUseCase {
    private final TransactionRepository transactionRepository;

    public GetTotalSpentByCategoryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "get-total-spent-by-category", description = "Calcula o valor total gasto em uma categoria e quantas transações a compõem")
    public TotalSpentOutput execute(@ToolParam(description = "Categoria de uma transação") Category category) {
        var transactions = transactionRepository.findAllByCategory(category);
        return TotalSpentOutput.from(category, transactions);
    }
}
