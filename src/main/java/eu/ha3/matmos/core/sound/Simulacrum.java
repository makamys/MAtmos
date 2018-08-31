package eu.ha3.matmos.core.sound;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import eu.ha3.easy.TimeStatistic;
import eu.ha3.matmos.MAtLog;
import eu.ha3.matmos.MAtMod;
import eu.ha3.matmos.MAtResourcePackDealer;
import eu.ha3.matmos.core.expansion.Expansion;
import eu.ha3.matmos.core.expansion.ExpansionManager;
import eu.ha3.matmos.core.expansion.VolumeUpdatable;
import eu.ha3.matmos.data.modules.ModuleRegistry;
import eu.ha3.matmos.game.user.VisualDebugger;
import eu.ha3.mc.haddon.supporting.SupportsFrameEvents;
import eu.ha3.mc.haddon.supporting.SupportsTickEvents;

public class Simulacrum implements SupportsTickEvents, SupportsFrameEvents {
    private ExpansionManager expansionManager;
    private ModuleRegistry dataGatherer;
    private VisualDebugger visualDebugger;
    private boolean isBrutallyInterrupted;

    private boolean hasResourcePacks;
    private boolean hasDisabledResourcePacks;

    public Simulacrum(MAtMod mod) {
        expansionManager = new ExpansionManager(new File(mod.util().getModsFolder(), "matmos/expansions_r29_userconfig/"), mod);
        expansionManager.setVolumeAndUpdate(mod.getConfig().getFloat("globalvolume.scale"));

        TimeStatistic stat = new TimeStatistic(Locale.ENGLISH);

        dataGatherer = new ModuleRegistry(mod);
        dataGatherer.load();
        visualDebugger = new VisualDebugger(mod, dataGatherer);
        expansionManager.setData(dataGatherer.getData());
        expansionManager.setCollector(dataGatherer);
        expansionManager.loadExpansions();

        hasResourcePacks = true;
        if (expansionManager.getExpansions().size() == 0) {
            MAtResourcePackDealer dealer = new MAtResourcePackDealer();
            if (dealer.findResourcePacks().size() == 0) {
                hasResourcePacks = false;
                hasDisabledResourcePacks = dealer.findDisabledResourcePacks().size() > 0;
            }
        }

        expansionManager.synchronize();

        MAtLog.info("Expansions loaded (" + stat.getSecondsAsString(1) + "s).");
    }

    public void interruptBrutally() {
        isBrutallyInterrupted = true;
    }

    public void dispose() {
        if (!isBrutallyInterrupted) {
            expansionManager.dispose();
        }
    }

    @Override
    public void onFrame(float semi) {
        expansionManager.onFrame(semi);
        visualDebugger.onFrame(semi);
    }

    @Override
    public void onTick() {
        dataGatherer.process();
        expansionManager.onTick();
    }

    public boolean hasResourcePacks() {
        return hasResourcePacks;
    }

    public boolean hasDisabledResourcePacks() {
        return hasDisabledResourcePacks;
    }

    public Map<String, Expansion> getExpansions() {
        return expansionManager.getExpansions();
    }

    public void synchronize() {
        expansionManager.synchronize();
    }

    public void saveConfig() {
        expansionManager.saveConfig();
    }

    public VisualDebugger getVisualDebugger() {
        return visualDebugger;
    }

    public VolumeUpdatable getGlobalVolumeControl() {
        return expansionManager;
    }

}
