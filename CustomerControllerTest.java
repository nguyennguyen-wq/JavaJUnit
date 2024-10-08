package com.javaAPI.spring.hibernate.controller;
import static org.mockito.BDDMockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaAPI.spring.hibernate.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.javaAPI.spring.hibernate.model.Customer;
import java.util.List;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(com.javaAPI.spring.hibernate.controller.CustomerController.class) // Load only the web layer
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CustomerRepository customerRepository; // Mock the repository
    private Customer customer;
    private Customer updatedCustomer;
    private List<Customer> customerList;

    @BeforeEach
    public void setup() {
        customer = new Customer();
        customer.setId(1L); // Mock ID
        customer.setFirstname("John");
        customer.setLastname("Doe");
        customer.setSocialsecuritynumber(12345);

        customerList = Arrays.asList(
                new Customer("Barak", "Obama", 12345),
                new Customer("Bill", "Gates", 123)
        );
    }

    @Test
    public void testGetAllCustomers_ReturnsCustomersList() throws Exception {
        when(customerRepository.findAll()).thenReturn(customerList);

        mockMvc.perform(get("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstname", is("Barak")))
                .andExpect(jsonPath("$[0].lastname", is("Obama")))
                .andExpect(jsonPath("$[0].socialsecuritynumber", is(12345)))
                .andExpect(jsonPath("$[1].firstname", is("Bill")))
                .andExpect(jsonPath("$[1].lastname", is("Gates")))
                .andExpect(jsonPath("$[1].socialsecuritynumber", is(123)));
    }

    @Test
    public void testUpdateCustomer_Success() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        String updatedCustomerJson = "{ \"firstname\": \"Bill\", \"lastname\": \"Gates\", \"socialsecuritynumber\": 12345 }";

        mockMvc.perform(put("/api/customers/1").contentType(MediaType.APPLICATION_JSON).content(updatedCustomerJson))
                .andExpect(status().isOk());
        assertThat(customer.getFirstname()).isEqualTo("Bill");
        assertThat(customer.getLastname()).isEqualTo("Gates");
        assertThat(customer.getSocialsecuritynumber()).isEqualTo(12345);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    public void testDeleteCustomer_Success() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        mockMvc.perform(delete("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testCreateCustomer() throws Exception {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        ObjectMapper objectMapper = new ObjectMapper();
        String customerJson = objectMapper.writeValueAsString(customer);
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.socialsecuritynumber").value(12345));
        verify(customerRepository, times(1)).save(any(Customer.class));
    }
}
