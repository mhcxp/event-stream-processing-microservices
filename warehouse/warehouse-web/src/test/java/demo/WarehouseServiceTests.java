package demo;

import demo.inventory.domain.Inventory;
import demo.inventory.domain.InventoryService;
import demo.inventory.event.InventoryEventService;
import demo.inventory.repository.InventoryRepository;
import demo.reservation.domain.Reservation;
import demo.reservation.domain.ReservationService;
import demo.reservation.event.ReservationEventService;
import demo.reservation.repository.ReservationRepository;
import demo.warehouse.domain.Warehouse;
import demo.warehouse.event.WarehouseEventService;
import demo.warehouse.repository.WarehouseRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class WarehouseServiceTests {

    @MockBean
    private InventoryEventService inventoryEventService;

    @MockBean
    private WarehouseEventService warehouseEventService;

    @MockBean
    private ReservationEventService reservationEventService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @MockBean
    private DiscoveryClient discoveryClient;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ReservationService reservationService;

    @Test
    public void saveReservationReturnsReservation() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setProductId("SKU-001");
        inventory = inventoryRepository.save(inventory);

        Inventory inventory1 = new Inventory();
        inventory1.setProductId("SKU-002");
        inventory1 = inventoryRepository.save(inventory1);

        Inventory inventory2 = new Inventory();
        inventory2.setProductId("SKU-003");
        inventory2 = inventoryRepository.save(inventory2);

        Reservation expected = new Reservation();
        expected.setInventory(inventory);
        expected = reservationRepository.save(expected);

        assertThat(expected.getIdentity()).isNotNull();

        Reservation actualReservation = reservationService.get(1L);
        Inventory actualInventory = inventoryService.get(1L);

        assertThat(actualReservation).isNotNull();
        assertThat(actualReservation.getInventory()).isNotNull();

        assertThat(actualInventory).isNotNull();

        Warehouse warehouse = new Warehouse();
        warehouse.setInventory(Arrays.asList(actualInventory, inventory1, inventory2));
        warehouse = warehouseRepository.save(warehouse);

        actualInventory.setWarehouse(warehouse);
        inventoryRepository.saveAndFlush(actualInventory);

        actualInventory = inventoryService.get(1L);
        assertThat(actualInventory.getWarehouse()).isNotNull();

        List<Warehouse> warehouseFound = warehouseRepository.findAllWithInventory(3L ,Arrays
                .asList("SKU-001", "SKU-002", "SKU-003"));

        List<Warehouse> warehouseNotFound = warehouseRepository.findAllWithInventory(2L, Arrays
                .asList("SKU-001", "SKU-099"));

    }

}