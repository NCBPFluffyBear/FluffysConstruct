package io.ncbpfluffybear.fluffysconstruct.data;

import io.ncbpfluffybear.fluffysconstruct.FCPlugin;
import io.ncbpfluffybear.fluffysconstruct.api.data.persistent.blockdata.BlockDataRepository;
import io.ncbpfluffybear.fluffysconstruct.items.specializeditems.smeltery.SearedTank;
import io.ncbpfluffybear.fluffysconstruct.setup.Molten;
import io.ncbpfluffybear.fluffysconstruct.utils.Keys;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SmelterySystem {

    private final UUID uuid;
    private final Location controller;

    private int maxVolume;
    private final Map<Molten.MoltenMaterial, Integer> contents;
    private Set<Location> bricks;
    private Set<Location> fuelTanks;
    private boolean active;

    public SmelterySystem(UUID uuid, Location controller) {
        this.uuid = uuid;
        this.controller = controller;
        this.bricks = new HashSet<>();
        this.fuelTanks = new HashSet<>();
        this.maxVolume = 0;
        this.active = false;
        this.contents = new LinkedHashMap<>();
        FCPlugin.getPersistenceUtils().markSmelteryDirty(uuid);
    }

    public int calculateTotalLava() {
        int totalLava = 0;
        for (Location location : fuelTanks) {
            totalLava += SearedTank.getLavaLevel(location);
        }
        return totalLava;
    }

    public int getCurrentVolume() {
        int currentVolume = 0;
        for (Integer volume : contents.values()) {
            currentVolume += volume;
        }

        return currentVolume;
    }

    /**
     * Adds melted items to contents.
     * @return if the products can fit
     */
    public boolean melt(List<Molten.Product> products, int multiplier) {
        int currentTotalVolume = getCurrentVolume(); // Total amount of molten material
        if (currentTotalVolume >= maxVolume) { // Already full
            return false;
        }

        // Check if new volume will fit
        int requiredVolume = products.stream().mapToInt(Molten.Product::volume).sum() * multiplier;
        if (requiredVolume + currentTotalVolume > maxVolume) {
            return false;
        }

        // Update volume for each material
        for (Molten.Product product : products) {
            contents.put(product.material(), contents.getOrDefault(product.material(), 0) + product.volume() * multiplier);
        }

        FCPlugin.getPersistenceUtils().markSmelteryDirty(uuid);
        return true;
    }

    /**
     * Links a system's UUID to the controller
     */
    private void assignSystemUUID(Location smelteryBlock) {
        BlockDataRepository.getOrCreateDataAt(smelteryBlock).set(Keys.SYSTEM_UUID, PersistentDataType.STRING, uuid.toString());
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
        FCPlugin.getPersistenceUtils().markSmelteryDirty(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void addBricks(Collection<Location> bricks) {
        this.bricks.addAll(bricks);
        bricks.forEach(this::assignSystemUUID);
        FCPlugin.getPersistenceUtils().markSmelteryDirty(uuid);
    }

    public void removeBrick(Location brick) {
        this.bricks.remove(brick);
    }

    public Set<Location> getBricks() {
        return bricks;
    }

    public void addFuelTanks(Collection<Location> fuelTanks) {
        this.fuelTanks.addAll(fuelTanks);
        fuelTanks.forEach(this::assignSystemUUID);
        FCPlugin.getPersistenceUtils().markSmelteryDirty(uuid);
    }

    public void removeFuelTank(Location fuelTank) {
        this.bricks.remove(fuelTank);
    }

    public int getFuel() {
        int totalFuel = 0;
        for (Location fuelTank : fuelTanks) {
            totalFuel += BlockDataRepository.getDataAt(fuelTank).getOrDefault(Keys.LAVA_LEVEL, PersistentDataType.INTEGER, 0);
        }
        return totalFuel;
    }

    public Set<Location> getFuelTanks() {
        return fuelTanks;
    }

    public Location getController() {
        return controller;
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    public void setMaxVolume(int maxVolume) {
        this.maxVolume = maxVolume;
        FCPlugin.getPersistenceUtils().markSmelteryDirty(uuid);
    }

    public Map<Molten.MoltenMaterial, Integer> getContents() {
        return contents;
    }

    public SmelterySystem(Map<String, Object> serialized) {
        this.controller = (Location) serialized.get("controller");
        this.uuid = UUID.fromString((String) serialized.get("id"));
        this.bricks = new HashSet<>((List<Location>) serialized.get("bricks"));
        this.fuelTanks = new HashSet<>((List<Location>) serialized.get("fuelTanks"));
        this.active = (boolean) serialized.get("active");

        this.contents = new LinkedHashMap<>();
        Map<String, Object> simpleContents = ((MemorySection) serialized.get("contents")).getValues(false);
        for (Map.Entry<String, Object> entry : simpleContents.entrySet()) {
            this.contents.put(Molten.MoltenMaterial.valueOf(entry.getKey()), (Integer) entry.getValue());
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("id", uuid.toString());
        serialized.put("controller", controller);
        serialized.put("bricks", new ArrayList<>(bricks));
        serialized.put("fuelTanks", new ArrayList<>(fuelTanks));
        serialized.put("active", active);

        Map<String, Integer> simpleContents = new LinkedHashMap<>();
        for (Map.Entry<Molten.MoltenMaterial, Integer> entry : contents.entrySet()) {
            simpleContents.put(entry.getKey().name(), entry.getValue());
        }
        serialized.put("contents", simpleContents);
        return serialized;
    }
}
