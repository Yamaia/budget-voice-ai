package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetTotalSpentByCategoryUseCaseTest {

    @Test
    void should_sumAllTransactions_when_categoryHasTransactions() {
        var transactionRepository = Mockito.mock(TransactionRepository.class);
        var useCase = new GetTotalSpentByCategoryUseCase(transactionRepository);

        var transactions = List.of(
                new Transaction("Mercado", 5000, Category.GROCERIES),
                new Transaction("Padaria", 1250, Category.GROCERIES));

        when(transactionRepository.findAllByCategory(Category.GROCERIES)).thenReturn(transactions);

        var output = useCase.execute(Category.GROCERIES);

        assertThat(output.category()).isEqualTo("GROCERIES");
        assertThat(output.total()).isEqualTo(62.50);
        assertThat(output.transactionCount()).isEqualTo(2);
    }

    @Test
    void should_returnZero_when_categoryHasNoTransactions() {
        var transactionRepository = Mockito.mock(TransactionRepository.class);
        var useCase = new GetTotalSpentByCategoryUseCase(transactionRepository);

        when(transactionRepository.findAllByCategory(Category.AUTO)).thenReturn(List.of());

        var output = useCase.execute(Category.AUTO);

        assertThat(output.total()).isEqualTo(0.0);
        assertThat(output.transactionCount()).isEqualTo(0);
    }
}
