package dio.budgeting.application.output;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record TotalSpentOutput(String category, double total, int transactionCount) {
    public static TotalSpentOutput from(Category category, List<Transaction> transactions) {
        long totalInCents = transactions.stream().mapToLong(Transaction::getAmount).sum();

        double total = BigDecimal.valueOf(totalInCents)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        return new TotalSpentOutput(category.name(), total, transactions.size());
    }
}
