package one.digitalinnovation.beerstock.controller;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.dto.QuantityDTO;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.service.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Collections;

import static one.digitalinnovation.beerstock.utils.JsonConvertionUtils.asJsonString;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

    private static final String BEER_API_URL_PATH = "/api/v1/beers";
    private static final long VALID_BEER_ID = 1L;
    private static final long INVALID_BEER_ID = 2l;
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

    private MockMvc mockMvc;

    @Mock
    private BeerService beerService;

    @InjectMocks
    private BeerController beerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
    void quandoPost_entaoCriaCerveja() throws Exception {
        //Preparação
        BeerDTO cervejaDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerService.createBeer(cervejaDTO)).thenReturn(cervejaDTO);

        //Quando-Então
        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cervejaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(cervejaDTO.getName())))
                .andExpect(jsonPath("$.brand", is(cervejaDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(cervejaDTO.getType().toString())));
    }

    @Test
    void quandoPostComErro_entaoMenssagemErro() throws Exception {
        //Preparação
        BeerDTO cervejaDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        cervejaDTO.setBrand(null);

        //Quando-Então
        mockMvc.perform(post(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cervejaDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void quandoGetChamadoNomeValido_entaoRetornaStatusOk() throws Exception {
        //Preparação
        BeerDTO cervejaDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerService.findByName(cervejaDTO.getName())).thenReturn(cervejaDTO);

        //Quando-Então
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + cervejaDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(cervejaDTO.getName())))
                .andExpect(jsonPath("$.brand", is(cervejaDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(cervejaDTO.getType().toString())));
    }

    @Test
    void quandoGetChamadoNomeInvalido_entaoRetornaExcecao() throws Exception {
        //Preparação
        BeerDTO cervejaDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerService.findByName(cervejaDTO.getName())).thenThrow(BeerNotFoundException.class);

        //Quando-Então
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + cervejaDTO.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void quandoGetChamaListaValido_entaoRetornaStatusOk() throws Exception {
        //Preparação
        BeerDTO cervejaDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerService.listAll()).thenReturn(Collections.singletonList(cervejaDTO));

        //Quando-Então
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(cervejaDTO.getName())))
                .andExpect(jsonPath("$[0].brand", is(cervejaDTO.getBrand())))
                .andExpect(jsonPath("$[0].type", is(cervejaDTO.getType().toString())));
    }

    @Test
    void quandoGetChamaListaInvalido_entaoRetornaExcecao() throws Exception {
        //Preparação
        when(beerService.listAll()).thenReturn(Collections.EMPTY_LIST);

        //Quando-Então
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void quandoDeleteChamadaValida_entaoRetornaStatusNoContent() throws Exception {
        //Preparação
        BeerDTO cervejaDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        doNothing().when(beerService).deleteById(cervejaDTO.getId());

        //Quando-Então
        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + cervejaDTO.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void quandoDeleteChamadaInvalida_entaoRetornaStatusNotFound() throws Exception {
        //Preparação
        doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);

        //Quando-Então
        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void quandoPatchChamaIncremento_entaoRetornaStatusOk() throws Exception {
        //Preparação
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(10)
                .build();
        BeerDTO cervejaDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        cervejaDTO.setQuantity(cervejaDTO.getQuantity() + quantityDTO.getQuantity());

        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(cervejaDTO);

        //Quando
        mockMvc.perform(MockMvcRequestBuilders.patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                //Então
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(cervejaDTO.getName())))
                .andExpect(jsonPath("$.brand", is(cervejaDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(cervejaDTO.getType().toString())))
                .andExpect(jsonPath("$.quantity", is(cervejaDTO.getQuantity())));
    }

    @Test
    void quandoPatchChamaIncrementoInvalido_entaoRetornaStatusBadReques() throws Exception {
        //Preparação
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(10)
                .build();
        BeerDTO cervejaDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        cervejaDTO.setQuantity(cervejaDTO.getQuantity() + quantityDTO.getQuantity());

        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerStockExceededException.class);

        //Quando
        mockMvc.perform(MockMvcRequestBuilders.patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                //Então
                .andExpect(status().isBadRequest());
    }

    //    @BeforeEach
//    void setUp() {
//        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
//                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
//                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
//                .build();
//    }
//
//    @Test
//    void whenPOSTIsCalledThenABeerIsCreated() throws Exception {
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);
//        mockMvc.perform(post(BEER_API_URL_PATH)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(beerDTO)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
//                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
//                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
//    }
//
//    @Test
//    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {
//        // given
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        beerDTO.setBrand(null);
//
//        // then
//        mockMvc.perform(post(BEER_API_URL_PATH)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(beerDTO)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
//        // given
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//
//        //when
//        when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);
//
//        // then
//        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + beerDTO.getName())
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
//                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
//                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
//    }
//
//    @Test
//    void whenGETIsCalledWithoutRegisteredNameThenNotFoundStatusIsReturned() throws Exception {
//        // given
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//
//        //when
//        when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);
//
//        // then
//        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + beerDTO.getName())
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void whenGETListWithBeersIsCalledThenOkStatusIsReturned() throws Exception {
//        // given
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//
//        //when
//        when(beerService.listAll()).thenReturn(Collections.singletonList(beerDTO));
//
//        // then
//        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].name", is(beerDTO.getName())))
//                .andExpect(jsonPath("$[0].brand", is(beerDTO.getBrand())))
//                .andExpect(jsonPath("$[0].type", is(beerDTO.getType().toString())));
//    }
//
//    @Test
//    void whenGETListWithoutBeersIsCalledThenOkStatusIsReturned() throws Exception {
//        // given
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//
//        //when
//        when(beerService.listAll()).thenReturn(Collections.singletonList(beerDTO));
//
//        // then
//        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void whenDELETEIsCalledWithValidIdThenNoContentStatusIsReturned() throws Exception {
//        // given
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//
//        //when
//        doNothing().when(beerService).deleteById(beerDTO.getId());
//
//        // then
//        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + beerDTO.getId())
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    void whenDELETEIsCalledWithInvalidIdThenNotFoundStatusIsReturned() throws Exception {
//        //when
//        doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);
//
//        // then
//        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void whenPATCHIsCalledToIncrementDiscountThenOKstatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(10)
//                .build();
//
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
//
//        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);
//
//        mockMvc.perform(MockMvcRequestBuilders.patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO))).andExpect(status().isOk())
//                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
//                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
//                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
//                .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
//    }
//
////    @Test
////    void whenPATCHIsCalledToIncrementGreatherThanMaxThenBadRequestStatusIsReturned() throws Exception {
////        QuantityDTO quantityDTO = QuantityDTO.builder()
////                .quantity(30)
////                .build();
////
////        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
////        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
////
////        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerStockExceededException.class);
////
////        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
////                .contentType(MediaType.APPLICATION_JSON)
////                .con(asJsonString(quantityDTO))).andExpect(status().isBadRequest());
////    }
//
////    @Test
////    void whenPATCHIsCalledWithInvalidBeerIdToIncrementThenNotFoundStatusIsReturned() throws Exception {
////        QuantityDTO quantityDTO = QuantityDTO.builder()
////                .quantity(30)
////                .build();
////
////        when(beerService.increment(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
////        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
////                .contentType(MediaType.APPLICATION_JSON)
////                .content(asJsonString(quantityDTO)))
////                .andExpect(status().isNotFound());
////    }
////
////    @Test
////    void whenPATCHIsCalledToDecrementDiscountThenOKstatusIsReturned() throws Exception {
////        QuantityDTO quantityDTO = QuantityDTO.builder()
////                .quantity(5)
////                .build();
////
////        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
////        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
////
////        when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);
////
////        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
////                .contentType(MediaType.APPLICATION_JSON)
////                .content(asJsonString(quantityDTO))).andExpect(status().isOk())
////                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
////                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
////                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
////                .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
////    }
////
////    @Test
////    void whenPATCHIsCalledToDEcrementLowerThanZeroThenBadRequestStatusIsReturned() throws Exception {
////        QuantityDTO quantityDTO = QuantityDTO.builder()
////                .quantity(60)
////                .build();
////
////        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
////        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
////
////        when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerStockExceededException.class);
////
////        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
////                .contentType(MediaType.APPLICATION_JSON)
////                .content(asJsonString(quantityDTO))).andExpect(status().isBadRequest());
////    }
////
////    @Test
////    void whenPATCHIsCalledWithInvalidBeerIdToDecrementThenNotFoundStatusIsReturned() throws Exception {
////        QuantityDTO quantityDTO = QuantityDTO.builder()
////                .quantity(5)
////                .build();
////
////        when(beerService.decrement(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
////        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
////                .contentType(MediaType.APPLICATION_JSON)
////                .content(asJsonString(quantityDTO)))
////                .andExpect(status().isNotFound());
////    }
}
