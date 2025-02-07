/*
    Copyright 2020, decal06, dan
    Gregicality

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gregification.opencomputers.drivers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DriverMultiblockRecipeLogic extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return MultiblockRecipeLogic.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof MetaTileEntityHolder) {
            return tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, side);
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof MetaTileEntityHolder) {
            IWorkable capability = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, side);
            if (capability instanceof MultiblockRecipeLogic)
                return new EnvironmentMultiblockRecipeLogic((MetaTileEntityHolder) tileEntity,
                        (MultiblockRecipeLogic) capability);
        }
        return null;
    }

    public final static class EnvironmentMultiblockRecipeLogic extends EnvironmentMetaTileEntity<MultiblockRecipeLogic> {

        public EnvironmentMultiblockRecipeLogic(MetaTileEntityHolder holder, MultiblockRecipeLogic capability) {
            super(holder, capability, "gtce_multiblockRecipeLogic");
        }

        @Callback(doc = "function():number --  Returns the amount of electricity contained in this Block, in EU units!")
        public Object[] getEnergyStored(final Context context, final Arguments args) {
            return new Object[] {tileEntity.getEnergyContainer().getEnergyStored()};
        }

        @Callback(doc = "function():number --  "
                + "Returns the amount of electricity containable in this Block, in EU units!")
        public Object[] getEnergyCapacity(final Context context, final Arguments args) {
            return new Object[] {tileEntity.getEnergyContainer().getEnergyCapacity()};
        }

        @Callback(doc = "function():number --  Gets the Output in EU/p.")
        public Object[] getOutputVoltage(final Context context, final Arguments args) {
            return new Object[] {tileEntity.getEnergyContainer().getOutputVoltage()};
        }

        @Callback(doc = "function():number -- Gets the amount of Energy Packets per tick.")
        public Object[] getOutputAmperage(final Context context, final Arguments args) {
            return new Object[] {tileEntity.getEnergyContainer().getOutputAmperage()};
        }

        @Callback(doc = "function():number -- Gets the maximum Input in EU/p.")
        public Object[] getInputVoltage(final Context context, final Arguments args) {
            return new Object[] {tileEntity.getEnergyContainer().getInputVoltage()};
        }

        @Callback(doc = "function():number -- Gets the amount of Energy Packets per tick.")
        public Object[] getInputAmperage(final Context context, final Arguments args) {
            return new Object[] {tileEntity.getEnergyContainer().getInputAmperage()};
        }

        @Nonnull
        private Object[] getInventory(IItemHandlerModifiable handler) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack itemStack = handler.getStackInSlot(slot);
                if (itemStack.isEmpty()) continue;
                Map<String, Object> map = new HashMap<>();
                map.put("count", itemStack.getCount());
                map.put("name", itemStack.getDisplayName());
                result.add(map);
            }
            return new Object[] {result};
        }

        @Callback(doc = "function():table -- Gets the Input Inventory.")
        public Object[] getInputInventory(final Context context, final Arguments args) {
            RecipeMapMultiblockController controller = (RecipeMapMultiblockController) tileEntity.getMetaTileEntity();
            return getInventory(controller.getInputInventory());
        }

        @Callback(doc = "function():table -- Gets the Output Inventory.")
        public Object[] getOutputInventory(final Context context, final Arguments args) {
            RecipeMapMultiblockController controller = (RecipeMapMultiblockController) tileEntity.getMetaTileEntity();
            return getInventory(controller.getOutputInventory());
        }

        @Nonnull
        private Object[] getTank(IMultipleTankHandler handler) {
            List<Map<String, Object>> result = new ArrayList<>();
            handler.getFluidTanks().forEach(tank->{
                Map<String, Object> map = new HashMap<>();
                FluidStack fluid = tank.getFluid();
                if (fluid == null){
                    map.put("amount", 0);
                    map.put("name", null);
                } else {
                    map.put("amount", fluid.amount);
                    map.put("name", fluid.getFluid().getName());
                }
                result.add(map);
            });
            return new Object[] {result};
        }

        @Callback(doc = "function():table -- Gets the Input Tank.")
        public Object[] getInputTank(final Context context, final Arguments args) {
            RecipeMapMultiblockController controller = (RecipeMapMultiblockController) tileEntity.getMetaTileEntity();
            return getTank(controller.getInputFluidInventory());
        }

        @Callback(doc = "function():table -- Gets the Output Tank.")
        public Object[] getOutputTank(final Context context, final Arguments args) {
            RecipeMapMultiblockController controller = (RecipeMapMultiblockController) tileEntity.getMetaTileEntity();
            return getTank(controller.getOutputFluidInventory());
        }
    }
}
