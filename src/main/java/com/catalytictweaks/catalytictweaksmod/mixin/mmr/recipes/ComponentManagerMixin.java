package com.catalytictweaks.catalytictweaksmod.mixin.mmr.recipes;

import com.google.common.cache.LoadingCache;
import es.degrassi.mmreborn.api.crafting.ComponentNotFoundException;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.ComponentManager;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Mixin(ComponentManager.class)
public abstract class ComponentManagerMixin {

    @Shadow private LoadingCache<ComponentType<?>, Map<IOType, List<MachineComponent<?>>>> fCV;
    @Shadow private MachineControllerEntity controller;

    @Overwrite
    public <C extends MachineComponent<T>, T> Optional<C> getComponent(ComponentType<T> type, IOType mode)
    {
        Map<IOType, List<MachineComponent<?>>> ioMap = fCV.asMap().get(type);

        if(ioMap == null)
        {
            try
            {
                ioMap = fCV.get(type);
            }
            catch(ExecutionException e)
            {
                // System.out.println("Error en FCV");
                throw new ComponentNotFoundException(controller.getFoundMachine(), type);
            }
        }

        if (ioMap == null){
            // System.out.println("IOMAP es null");
            return Optional.empty();
        }

        List<MachineComponent<?>> list = ioMap.get(mode);
        if (list == null || list.isEmpty())
        {
            if(type.getId().toString().contains("parallel"))
            {
                list = new ArrayList<>();
                for (List<MachineComponent<?>> subList : ioMap.values()) {
                    if (subList != null) list.addAll(subList);
                }
            }
        }

        if (list == null || list.isEmpty()){
            // System.out.println("List esta vacio o null");
            // System.out.println(type.getId().toString());
            // System.out.println(mode);
            // System.out.println(list==null);
            // if(list != null)
            // {
            //     System.out.println(list.isEmpty());
            // }
            return Optional.empty();
        }

        int size = list.size();

        if(size == 1)
        {
            MachineComponent<?> c = list.get(0);
            // if(c == null)
            // {
            //     System.out.println("Tamaño 1: C es null");
            // }
            return c == null ? Optional.empty() : Optional.of((C) c);
        }

        MachineComponent<?>[] tempArray = new MachineComponent[size];
        int validCount = 0;

        for(int i = 0; i < size; i++)
        {
            MachineComponent<?> c = list.get(i);
            if(c != null)
            {
                tempArray[validCount++] = c;
            }
        }

        if (validCount == 0){
            // System.out.println("Valid Count es 0");
            return Optional.empty();
        }

        Arrays.sort(tempArray, 0, validCount);

        C accumulator = (C) tempArray[0];

        for(int i = 1; i < validCount; i++)
        {
            C current = (C) tempArray[i];
            if(accumulator.canMerge(current))
            {
                accumulator = (C) accumulator.merge(current);
            }
        }
        return Optional.of(accumulator);
    }
}