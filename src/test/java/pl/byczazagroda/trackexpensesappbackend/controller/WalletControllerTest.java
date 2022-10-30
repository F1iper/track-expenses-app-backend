package pl.byczazagroda.trackexpensesappbackend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.byczazagroda.trackexpensesappbackend.dto.CreateWalletDTO;
import pl.byczazagroda.trackexpensesappbackend.dto.WalletDTO;
import pl.byczazagroda.trackexpensesappbackend.mapper.WalletModelMapper;
import pl.byczazagroda.trackexpensesappbackend.service.WalletService;
import pl.byczazagroda.trackexpensesappbackend.service.WalletServiceImpl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@WebMvcTest(controllers = WalletController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WalletServiceImpl.class),
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                WalletModelMapper.class}))
class WalletControllerTest {

    private static final Long ID_OF_WALLET_1 = 1L;

    private static final Long ID_OF_WALLET_2 = 2L;

    private static final Long ID_OF_WALLET_3 = 3L;

    private static final String NAME_OF_WALLET_1 = "nameOfWallet1";

    private static final String NAME_OF_WALLET_2 = "nameOfWallet2";

    private static final String NAME_OF_WALLET_3 = "nameOfWallet3";

    private static final Instant CREATION_DATE_OF_WALLET_1 = Instant.parse("2022-09-24T19:09:35.573036Z");

    private static final Instant CREATION_DATE_OF_WALLET_2 = Instant.parse("2022-09-25T17:10:39.684145Z");

    private static final Instant CREATION_DATE_OF_WALLET_3 = Instant.parse("2022-09-26T18:11:49.132454Z");

    private static final String LIST_OF_WALLETS_HEADER_MSG = "The list of wallets has been successfully retrieved.";

    private static final String EMPTY_LIST_OF_WALLETS_HEADER_MSG = "There are no available wallets to view.";

    @MockBean
    private WalletService walletService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldThrowAnExceptionWhenNameIsEmpty() throws Exception {
        // given
        CreateWalletDTO createWalletDTO = new CreateWalletDTO("");
        given(walletService.createWallet(createWalletDTO))
                .willReturn(new WalletDTO(1L, "", Instant.now()));

        // when
        ResultActions result = mockMvc.perform(post("/api/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createWalletDTO))));
        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowAnExceptionWhenNameIsNull() throws Exception {
        // given
        CreateWalletDTO createWalletDTO = new CreateWalletDTO(null);
        given(walletService.createWallet(createWalletDTO))
                .willReturn(new WalletDTO(1L, null, Instant.now()));

        // when
        ResultActions result = mockMvc.perform(post("/api/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createWalletDTO))));
        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowAnExceptionWhenNameIsTooLong() throws Exception {
        // given
        String walletName = "This wallet name is too long, it contains over 20 characters";
        CreateWalletDTO createWalletDTO = new CreateWalletDTO(walletName);
        given(walletService.createWallet(createWalletDTO))
                .willReturn(
                        new WalletDTO(1L, walletName, Instant.now()));

        // when
        ResultActions result = mockMvc.perform(post("/api/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createWalletDTO))));
        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowAnExceptionWhenNameContainsIllegalLetters() throws Exception {
        // given
        CreateWalletDTO createWalletDTO = new CreateWalletDTO("#$@");
        given(walletService.createWallet(createWalletDTO))
                .willReturn(new WalletDTO(1L, "#$@", Instant.now()));

        // when
        ResultActions result = mockMvc.perform(post("/api/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createWalletDTO))));
        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return empty list of wallets and http ok status")
    void shouldReturnEmptyList() throws Exception {
        // when
        MockHttpServletResponse result = mockMvc.perform(get("/api/wallet")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse();

        // then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(result.getContentAsString()).isEqualTo(Collections.emptyList().toString());
        assertThat(result.getHeader("message")).isEqualTo(EMPTY_LIST_OF_WALLETS_HEADER_MSG);
    }

    @Test
    @DisplayName("Should return information about all wallets and http ok status")
    void shouldReturnListOfAllWallets() throws Exception {
        // given
        List<WalletDTO> listOfWalletsDTO = createListOfWalletsDTO();
        given(walletService.getWallets()).willReturn(listOfWalletsDTO);

        // then
        mockMvc.perform(get("/api/wallet")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("message", LIST_OF_WALLETS_HEADER_MSG))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(ID_OF_WALLET_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(NAME_OF_WALLET_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].creationDate")
                        .value(CREATION_DATE_OF_WALLET_1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(ID_OF_WALLET_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(NAME_OF_WALLET_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].creationDate")
                        .value(CREATION_DATE_OF_WALLET_2.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].id").value(ID_OF_WALLET_3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].name").value(NAME_OF_WALLET_3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].creationDate")
                        .value(CREATION_DATE_OF_WALLET_3.toString()));
    }

    private List<WalletDTO> createListOfWalletsDTO() {
        WalletDTO walletDTO1 = new WalletDTO(ID_OF_WALLET_1, NAME_OF_WALLET_1, CREATION_DATE_OF_WALLET_1);
        WalletDTO walletDTO2 = new WalletDTO(ID_OF_WALLET_2, NAME_OF_WALLET_2, CREATION_DATE_OF_WALLET_2);
        WalletDTO walletDTO3 = new WalletDTO(ID_OF_WALLET_3, NAME_OF_WALLET_3, CREATION_DATE_OF_WALLET_3);
        return List.of(walletDTO1, walletDTO2, walletDTO3);
    }
}