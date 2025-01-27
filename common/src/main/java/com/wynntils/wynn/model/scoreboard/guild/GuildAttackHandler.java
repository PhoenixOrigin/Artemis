/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard.guild;

import com.wynntils.wynn.model.GuildAttackTimerModel;
import com.wynntils.wynn.model.scoreboard.ScoreboardHandler;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;
import com.wynntils.wynn.model.scoreboard.Segment;

public class GuildAttackHandler implements ScoreboardHandler {
    @Override
    public void onSegmentChange(Segment newValue, ScoreboardModel.SegmentType segmentType) {
        GuildAttackTimerModel.processChanges(newValue);
    }

    @Override
    public void onSegmentRemove(Segment segment, ScoreboardModel.SegmentType segmentType) {
        GuildAttackTimerModel.resetTimers();
    }

    @Override
    public void resetHandler() {
        GuildAttackTimerModel.resetTimers();
    }
}
