package com.catalytictweaks.catalytictweaksmod.mmr;

import es.degrassi.mmreborn.common.util.IEnergyHandler;

import java.util.ArrayList;
import java.util.List;

public class CompositeEnergyHandler implements IEnergyHandler {
    private final List<IEnergyHandler> handlers = new ArrayList<>();

    public CompositeEnergyHandler(IEnergyHandler... initialHandlers) {
        for (IEnergyHandler h : initialHandlers) {
            addContent(h);
        }
    }

    public void addContent(IEnergyHandler handler) {
        if (handler instanceof CompositeEnergyHandler composite) {
            this.handlers.addAll(composite.handlers);
        } else {
            this.handlers.add(handler);
        }
    }

    @Override
    public long getCurrentEnergy() {
        long total = 0;
        for (IEnergyHandler h : handlers) {
            total += h.getCurrentEnergy();
        }
        return total;
    }

    @Override
    public void setCurrentEnergy(long energy) {
    }

    @Override
    public long getMaxEnergy(){
        long total = 0;
        for (IEnergyHandler h : handlers) {
            total += h.getMaxEnergy();
        }
        return total;
    }

    @Override
    public void setCanExtract(boolean b) {
        for (IEnergyHandler h : handlers) h.setCanExtract(b);
    }

    @Override
    public void setCanInsert(boolean b) {
        for (IEnergyHandler h : handlers) h.setCanInsert(b);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int receivedTotal = 0;
        int remaining = maxReceive;

        for (IEnergyHandler h : handlers) {
            if (remaining <= 0) break;
            
            int received = h.receiveEnergy(remaining, simulate);
            receivedTotal += received;
            remaining -= received;
        }
        return receivedTotal;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extractedTotal = 0;
        int remaining = maxExtract;

        for (IEnergyHandler h : handlers) {
            if (remaining <= 0) break;

            int extracted = h.extractEnergy(remaining, simulate);
            extractedTotal += extracted;
            remaining -= extracted;
        }
        return extractedTotal;
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, getCurrentEnergy());
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, getMaxEnergy());
    }

    @Override
    public boolean canExtract() {
        for (IEnergyHandler h : handlers) {
            if (h.canExtract()) return true;
        }
        return false;
    }

    @Override
    public boolean canReceive() {
        for (IEnergyHandler h : handlers) {
            if (h.canReceive()) return true;
        }
        return false;
    }
    
    // Método auxiliar para obtener la lista interna (para fusiones futuras)
    public List<IEnergyHandler> getHandlers() {
        return handlers;
    }
}
