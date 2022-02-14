package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
    void quandoInformarCerveja_entaoDeveSerCriada() throws BeerAlreadyRegisteredException {
        //Preparação
        BeerDTO cervejaDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer cervejaParaSalvar = beerMapper.toModel(cervejaDTO);

        Mockito.when(beerRepository.findByName(cervejaDTO.getName())).thenReturn(Optional.empty());
        Mockito.when(beerRepository.save(cervejaParaSalvar)).thenReturn(cervejaParaSalvar);

        //Quando
        BeerDTO cervejaCriada = beerService.createBeer(cervejaDTO);

        //Então
        assertThat(cervejaDTO.getId(), is(equalTo(cervejaCriada.getId())));
        assertThat(cervejaDTO.getName(), is(equalTo(cervejaCriada.getName())));
        assertThat(cervejaDTO.getQuantity(), is(equalTo(cervejaCriada.getQuantity())));
    }

    @Test
    void quandoInformaCervejaJaCriada_entaoLancaException() throws BeerAlreadyRegisteredException {
        //Preparação
        BeerDTO cervejaExperada = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer cervejaDuplicada = beerMapper.toModel(cervejaExperada);

        Mockito.when(beerRepository.findByName(cervejaExperada.getName())).thenReturn(Optional.of(cervejaDuplicada));

        //Quando-Então
        assertThrows(BeerAlreadyRegisteredException.class, () ->beerService.createBeer(cervejaExperada));
    }

    @Test
    void quandoRequisitadoNomeValido_entaoRetornaCerveja() throws BeerNotFoundException {
        //Preparação
        BeerDTO cervejaExperadaDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer cervejaExperada = beerMapper.toModel(cervejaExperadaDTO);

        when(beerRepository.findByName(cervejaExperada.getName())).thenReturn(Optional.of(cervejaExperada));

        //Quando
        BeerDTO cervejaProcurada = beerService.findByName(cervejaExperadaDTO.getName());

        //Então
        assertThat(cervejaProcurada, is(equalTo(cervejaExperadaDTO)));
    }

    @Test
    void quandoRequisitadoNomeInvalido_entaoRetornaExceção() {
        //Preparação
        BeerDTO cervejaExperadaDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerRepository.findByName(cervejaExperadaDTO.getName())).thenReturn(Optional.empty());

        //Quando-então
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(cervejaExperadaDTO.getName()));

    }

    @Test
    void quandoChamadaListaCervejas_entaoRetornaListaCervejas() {
        //Preparação
        BeerDTO cervejaExperadaDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer cervejaExperada = beerMapper.toModel(cervejaExperadaDTO);

        when(beerRepository.findAll()).thenReturn(Collections.singletonList(cervejaExperada));

        //Quando
        List<BeerDTO> listaProcurada = beerService.listAll();

        //Então
        assertThat(listaProcurada, is(not(empty())));
        assertThat(listaProcurada.get(0), is(equalTo(cervejaExperadaDTO)));
    }

    @Test
    void quandoChamaListaCervejasVazia_entaoRetornaExcecao() {
        //Preparação
        BeerDTO cervejaExperadaDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

        //Quando
        List<BeerDTO> listaProcurada = beerService.listAll();

        //Então
        assertThat(listaProcurada, is(empty()));
    }

    @Test
    void quandoDeletaCervejaValida_entaoCervejaDeletada() throws BeerNotFoundException {
        //Preparação
        BeerDTO cervejaExperadaDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer cervejaExperada = beerMapper.toModel(cervejaExperadaDTO);

        when(beerRepository.findById(cervejaExperadaDTO.getId())).thenReturn(Optional.of(cervejaExperada));
        doNothing().when(beerRepository).deleteById(cervejaExperadaDTO.getId());

        //Quando
        beerService.deleteById(cervejaExperadaDTO.getId());
        verify(beerRepository, times(1)).findById(cervejaExperadaDTO.getId());
        verify(beerRepository, times(1)).deleteById(cervejaExperadaDTO.getId());
    }

    @Test
    void quandoDeletaCervejaInvalida_entaoRetornaStatusOk() throws BeerNotFoundException {
        //Preparação
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        //Quando-Então
        assertThrows(BeerNotFoundException.class, () -> beerService.deleteById(INVALID_BEER_ID));
    }

    @Test
    void quandoIncrementaCerveja() throws BeerNotFoundException, BeerStockExceededException {
        //Preparação
        BeerDTO cervejaExperadaDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer cervejaExperada = beerMapper.toModel(cervejaExperadaDTO);

        when(beerRepository.findById(cervejaExperadaDTO.getId())).thenReturn(Optional.of(cervejaExperada));
        when(beerRepository.save(cervejaExperada)).thenReturn(cervejaExperada);

        int quantidadeIncremento = 10;
        int quantidadeIncrementada = cervejaExperadaDTO.getQuantity() + quantidadeIncremento;

        //Quando
        BeerDTO cervejaIncrementada = beerService.increment(cervejaExperada.getId(), quantidadeIncremento);

        //Então
        assertThat(quantidadeIncrementada, equalTo(cervejaIncrementada.getQuantity()));
        assertThat(quantidadeIncrementada, lessThan(cervejaIncrementada.getMax()));
    }

    @Test
    void quandoIncrementaCervejaAlemDoMaximo() {
        //Preparação
        BeerDTO cervejaExperadaDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer cervejaExperada = beerMapper.toModel(cervejaExperadaDTO);

        when(beerRepository.findById(cervejaExperadaDTO.getId())).thenReturn(Optional.of(cervejaExperada));

        int quantidadeIncremento = 45;

        //Quando-então
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(cervejaExperadaDTO.getId(), quantidadeIncremento));
    }

    @Test
    void quandoIncrementoChamaIdInvalido_entaoLancaExcecao() {
        //Preparação
        int quantidadeIncremento = 45;

        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        //Quando-então
        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantidadeIncremento));
    }

    //    @Test
//    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
//        // given
//        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);
//
//        // when
//        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
//        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);
//
//        //then
//        BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);
//
//        assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
//        assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
//        assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));
//    }
//
//    @Test
//    void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() {
//        // given
//        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);
//
//        // when
//        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));
//
//        // then
//        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
//    }
//
//    @Test
//    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
//        // given
//        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
//
//        // when
//        when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));
//
//        // then
//        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());
//
//        assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
//    }
//
//    @Test
//    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {
//        // given
//        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//
//        // when
//        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());
//
//        // then
//        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
//    }
//
//    @Test
//    void whenListBeerIsCalledThenReturnAListOfBeers() {
//        // given
//        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
//
//        //when
//        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));
//
//        //then
//        List<BeerDTO> foundListBeersDTO = beerService.listAll();
//
//        assertThat(foundListBeersDTO, is(not(empty())));
//        assertThat(foundListBeersDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
//    }
//
//    @Test
//    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
//        //when
//        when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
//
//        //then
//        List<BeerDTO> foundListBeersDTO = beerService.listAll();
//
//        assertThat(foundListBeersDTO, is(empty()));
//    }
//
//    @Test
//    void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException{
//        // given
//        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);
//
//        // when
//        when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
//        doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());
//
//        // then
//        beerService.deleteById(expectedDeletedBeerDTO.getId());
//
//        verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
//        verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
//    }
//
//    @Test
//    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
//        //given
//        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
//
//        //when
//        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
//        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
//
//        int quantityToIncrement = 10;
//        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;
//
//        // then
//        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);
//
//        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
//        assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
//    }
//
//    @Test
//    void whenIncrementIsGreatherThanMaxThenThrowException() {
//        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
//
//        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
//
//        int quantityToIncrement = 80;
//        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
//    }
//
//    @Test
//    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() {
//        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
//
//        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
//
//        int quantityToIncrement = 45;
//        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
//    }
//
//    @Test
//    void whenIncrementIsCalledWithInvalidIdThenThrowException() {
//        int quantityToIncrement = 10;
//
//        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
//
//        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
//    }
////
////    @Test
////    void whenDecrementIsCalledThenDecrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
////        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
////        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
////
////        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
////        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
////
////        int quantityToDecrement = 5;
////        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
////        BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);
////
////        assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
////        assertThat(expectedQuantityAfterDecrement, greaterThan(0));
////    }
////
////    @Test
////    void whenDecrementIsCalledToEmptyStockThenEmptyBeerStock() throws BeerNotFoundException, BeerStockExceededException {
////        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
////        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
////
////        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
////        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
////
////        int quantityToDecrement = 10;
////        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
////        BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);
////
////        assertThat(expectedQuantityAfterDecrement, equalTo(0));
////        assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
////    }
////
////    @Test
////    void whenDecrementIsLowerThanZeroThenThrowException() {
////        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
////        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
////
////        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
////
////        int quantityToDecrement = 80;
////        assertThrows(BeerStockExceededException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
////    }
////
////    @Test
////    void whenDecrementIsCalledWithInvalidIdThenThrowException() {
////        int quantityToDecrement = 10;
////
////        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
////
////        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, quantityToDecrement));
////    }
}
