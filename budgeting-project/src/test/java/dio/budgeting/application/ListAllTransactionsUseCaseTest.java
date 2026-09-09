package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListAllTransactionsUseCaseTest {

    @Test
    void should_returnAllTransactions_regardlessOfCategory() {
        var transactionRepository = Mockito.mock(TransactionRepository.class);
        var useCase = new ListAllTransactionsUseCase(transactionRepository);

        var transactions = List.of(
                new Transaction("Mercado", 5000, Category.GROCERIES),
                new Transaction("Remédio", 3000, Category.PHARMA),
                new Transaction("Combustível", 20000, Category.AUTO));

        when(transactionRepository.findAll()).thenReturn(transactions);

        var output = useCase.execute();

        assertThat(output).hasSize(3);
        assertThat(output).extracting("category")
                .containsExactlyInAnyOrder("GROCERIES", "PHARMA", "AUTO");
    }

    @Test
    void should_returnEmptyList_when_noTransactionsExist() {
        var transactionRepository = Mockito.mock(TransactionRepository.class);
        var useCase = new ListAllTransactionsUseCase(transactionRepository);

        when(transactionRepository.findAll()).thenReturn(List.of());

        var output = useCase.execute();

        assertThat(output).isEmpty();
    }
}
