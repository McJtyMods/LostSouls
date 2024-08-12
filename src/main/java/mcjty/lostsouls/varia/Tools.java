package mcjty.lostsouls.varia;

import mcjty.lostsouls.data.MobSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class Tools {

    public static ResourceLocation getRandomFromList(RandomSource random, List<MobSettings.RL> list) {
        if (list.isEmpty()) {
            return null;
        }
        List<MobSettings.RL> elements = new ArrayList<>();
        float totalweight = 0;
        for (MobSettings.RL pair : list) {
            elements.add(pair);
            totalweight += pair.weight();
        }
        float r = random.nextFloat() * totalweight;
        for (MobSettings.RL pair : elements) {
            r -= pair.weight();
            if (r <= 0) {
                return pair.name();
            }
        }
        return null;
    }


}
