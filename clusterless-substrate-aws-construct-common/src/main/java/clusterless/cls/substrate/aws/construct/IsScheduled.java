package clusterless.cls.substrate.aws.construct;

import clusterless.cls.substrate.aws.resources.Rules;
import clusterless.commons.temporal.IntervalUnits;
import org.slf4j.Logger;
import software.amazon.awscdk.services.events.CronOptions;
import software.amazon.awscdk.services.events.IRuleTarget;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.constructs.Construct;

import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;

public interface IsScheduled {
    /**
     * Verify the interval is a valid temporal unit
     *
     * @param interval as a string
     * @return the temporal unit
     */
    default TemporalUnit verifiedTemporalUnit(String interval) {
        // confirm unit exits
        TemporalUnit temporalUnit = IntervalUnits.find(interval);
        IntervalUnits.verifyHasFormatter(temporalUnit);
        return temporalUnit;
    }

    default void createScheduledRule(Logger log, Construct construct, String modelName, String scheduleDeclaration, IRuleTarget ruleTarget, boolean enabled) {
        Schedule schedule = createScheduleFrom(scheduleDeclaration);

        createScheduledRule(log, construct, modelName, ruleTarget, schedule, enabled);
    }

    default Schedule createScheduleFrom(String scheduleDeclaration) {
        // https://docs.aws.amazon.com/scheduler/latest/UserGuide/schedule-types.html#cron-based
        Schedule schedule;

        Optional<TemporalUnit> temporalUnit = IntervalUnits.findSafe(scheduleDeclaration);

        if (temporalUnit.isPresent()) {
            if (temporalUnit.get().getDuration().toMinutes() > 60) {
                throw new UnsupportedOperationException("temporal unit greater than 60 minutes, consider using a cron expression: " + temporalUnit);
            }
            schedule = scheduleFromTemporalUnit(temporalUnit.get())
                    .orElseThrow(() -> new UnsupportedOperationException("unsupported temporal unit: " + temporalUnit));
        } else {
            // cron(...) or rate(...) expression
            schedule = Schedule.expression(scheduleDeclaration);
        }

        return schedule;
    }

    default Optional<Schedule> scheduleFromTemporalUnit(TemporalUnit temporalUnit) {
        if (temporalUnit.getDuration().toMinutes() > 60) {
            return Optional.empty();
        }

        String cronMinute = String.format("0/%d", temporalUnit.getDuration().toMinutes());

        return Optional.of(Schedule.cron(CronOptions.builder()
                .minute(cronMinute)
                .build()));
    }

    default void createScheduledRule(Logger log, Construct construct, String modelName, IRuleTarget ruleTarget, Schedule schedule, boolean enabled) {
        log.info("creating rule schedule with: {}", schedule.getExpressionString());

        String listenerRuleName = Rules.ruleName(construct, modelName).lowerHyphen();

        Rule.Builder.create(construct, "ListenerEvent")
                .ruleName(listenerRuleName)
                .enabled(enabled)
                .schedule(schedule)
                .targets(List.of(ruleTarget))
                .build();
    }
}
