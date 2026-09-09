package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PersistTransactionUseCaseTest {

    private TransactionRepository transactionRepository;
    private PersistTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        transactionRepository = Mockito.mock(TransactionRepository.class);
        useCase = new PersistTransactionUseCase(transactionRepository);
    }

    @Test
    void should_persistTransaction_when_inputIsValid() {
        var input = new PersistTransactionInput("Compras no mercado", 5000, Category.GROCERIES);
        var savedTransaction = new Transaction("Compras no mercado", 5000, Category.GROCERIES);

        when(transactionRepository.save(any())).thenReturn(savedTransaction);

        var output = useCase.execute(input);

        assertThat(output.description()).isEqualTo("Compras no mercado");
        assertThat(output.category()).isEqualTo("GROCERIES");
        verify(transactionRepository).save(any());
    }

    @Test
    void should_throwException_when_descriptionIsBlank() {
        var input = new PersistTransactionInput("   ", 5000, Category.GROCERIES);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("descrição");

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void should_throwException_when_amountIsZeroOrNegative() {
        var input = new PersistTransactionInput("Farmácia", 0, Category.PHARMA);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("valor");

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void should_throwException_when_categoryIsNull() {
        var input = new PersistTransactionInput("Posto de gasolina", 15000, null);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("categoria");

        verifyNoInteractions(transactionRepository);
    }
}
