package com.tarang.elite.data.content

import java.time.DayOfWeek

data class ExerciseSpec(
    val id: String,
    val name: String,
    val sets: Int,
    val reps: String,
    val equipment: String,
    val notes: String,
    val altName: String,
    val altReps: String,
    val altNotes: String,
    val howTo: String,
)

data class WorkoutSpec(
    val key: String,
    val name: String,
    val duration: String,
    val intensity: String,
    val goal: String,
    val exercises: List<ExerciseSpec>,
)

enum class DayKind { WORKOUT, CLASS, REST }

data class DayPlan(
    val dayLabel: String,
    val title: String,
    val emoji: String,
    val duration: String,
    val intensity: String,
    val kind: DayKind,
    val workoutKey: String? = null,
    val priority: Boolean = false,
)

object WorkoutLibrary {

    val tuesday = WorkoutSpec(
        key = "tuesday",
        name = "Tuesday — Pre-Bed Lower Body",
        duration = "15–20 mins",
        intensity = "Moderate (65–75%)",
        goal = "Lower body maintenance without disrupting sleep",
        exercises = listOf(
            ExerciseSpec(
                "goblet-squat", "Goblet Squats", 3, "15", "8kg Kettlebell",
                "Keep chest up, squat to depth",
                "Bodyweight Squats", "20", "Slow and controlled",
                "Hold kettlebell at chest, feet shoulder-width, push knees out"
            ),
            ExerciseSpec(
                "band-glute-bridge", "Resistance Band Glute Bridges", 3, "15 each leg", "Resistance Band",
                "Squeeze glutes at top, hold 2 seconds",
                "Double Leg Glute Bridges", "20", "No band needed",
                "Band above knees, drive through heels, squeeze at top"
            ),
            ExerciseSpec(
                "sl-rdl", "Single-Leg Romanian Deadlifts", 3, "10 each leg", "Bodyweight",
                "Feel hamstring stretch, keep back flat",
                "Good Mornings", "15", "Both feet planted",
                "Hinge at hip, reach opposite hand to floor, slight knee bend"
            ),
            ExerciseSpec(
                "wall-sit", "Wall Sit", 3, "45 seconds", "Wall",
                "Thighs parallel to floor, back flat against wall",
                "Wall Sit (higher)", "30 seconds", "Higher position",
                "Back against wall, slide down to 90°, hold position"
            ),
            ExerciseSpec(
                "dead-bugs", "Dead Bugs", 3, "10 each side", "None",
                "Keep lower back pressed to floor",
                "Bird Dogs", "8 each side", "On hands and knees",
                "Arms up, opposite arm and leg extend, return slowly"
            ),
        )
    )

    val thursday = WorkoutSpec(
        key = "thursday",
        name = "Thursday — GYM LEG DAY ⭐",
        duration = "45–55 mins",
        intensity = "High (85–90%)",
        goal = "BUILD THOSE LEGS — your priority session",
        exercises = listOf(
            ExerciseSpec(
                "barbell-box-squat", "Barbell Box Squat", 4, "6–8", "Barbell + Box",
                "Progress from 40kg — add 2.5kg when you hit 8 reps",
                "Goblet Box Squat", "10–12", "Use heavy dumbbell",
                "Bar on upper back, sit back to box, pause, drive up explosively"
            ),
            ExerciseSpec(
                "leg-press", "Leg Press", 3, "10–12", "Leg Press Machine",
                "Go heavy, full depth, don't lock knees at top",
                "Bulgarian Split Squat", "10 each leg", "Rear foot elevated",
                "Feet shoulder-width on platform, lower until 90°, press through heels"
            ),
            ExerciseSpec(
                "romanian-deadlift", "Romanian Deadlift", 3, "8–10", "Barbell or Dumbbells",
                "Feel the hamstring stretch, keep bar close to legs",
                "Kettlebell RDL", "12", "Single or double kettlebell",
                "Hinge at hips, push hips back, slight knee bend, feel hamstring stretch"
            ),
            ExerciseSpec(
                "walking-lunges", "Walking Lunges", 3, "12 each leg", "Bodyweight or Dumbbells",
                "Big steps, knee tracks over toe",
                "Reverse Lunges", "10 each leg", "Stationary, step back",
                "Step forward, lower back knee toward floor, push through front heel"
            ),
            ExerciseSpec(
                "leg-curl", "Leg Curl Machine", 3, "12–15", "Leg Curl Machine",
                "Squeeze at the top, slow negative",
                "Nordic Curl Negatives", "5–6", "Lower slowly, push back up",
                "Curl heels toward glutes, squeeze at top, 3-second negative"
            ),
            ExerciseSpec(
                "calf-raises", "Standing Calf Raises", 3, "15–20", "Calf Raise Machine or Step",
                "Full stretch at bottom, pause at top",
                "Single Leg Calf Raises", "12 each", "Bodyweight on step",
                "Rise onto toes, squeeze at top, lower below platform level"
            ),
        )
    )

    val weekendHome = WorkoutSpec(
        key = "weekendHome",
        name = "Weekend Home Session (Kids Weekend)",
        duration = "20–25 mins",
        intensity = "Moderate (70–80%)",
        goal = "Maintain momentum with circuit training",
        exercises = listOf(
            ExerciseSpec(
                "kb-swings", "Kettlebell Swings", 3, "20", "8kg Kettlebell",
                "Hip hinge, snap hips forward, arms are just along for the ride",
                "Bodyweight Hip Thrusts", "25", "Fast and explosive",
                "Hike kettlebell back, snap hips, let momentum swing bell to chest height"
            ),
            ExerciseSpec(
                "press-ups", "Press-Ups", 3, "15 (or max)", "None",
                "Full range, chest to floor, elbows at 45°",
                "Incline Press-Ups", "20", "Hands on elevated surface",
                "Hands outside shoulders, lower chest to floor, push back up"
            ),
            ExerciseSpec(
                "reverse-lunges-home", "Reverse Lunges", 3, "12 each leg", "Bodyweight or Kettlebell",
                "Step back, lower knee toward floor",
                "Bodyweight Squats", "20", "Slow tempo",
                "Step back into lunge, lower back knee, push through front heel to return"
            ),
            ExerciseSpec(
                "band-rows", "Resistance Band Rows", 3, "15", "Resistance Band",
                "Squeeze shoulder blades together, pause at contraction",
                "Doorframe Rows", "12", "Hold doorframe, lean back, pull chest to door",
                "Anchor band at chest height, pull elbows back, squeeze shoulder blades"
            ),
            ExerciseSpec(
                "plank-home", "Plank Hold", 3, "45 seconds", "None",
                "Keep body straight, squeeze glutes, breathe",
                "Knee Plank", "60 seconds", "Knees on floor",
                "Forearms on floor, body in straight line, no sagging or piking"
            ),
        )
    )

    val weekendGym = WorkoutSpec(
        key = "weekendGym",
        name = "Weekend Gym Session (Free Weekend)",
        duration = "40–50 mins",
        intensity = "Moderate-High (80%)",
        goal = "Full body training with weak point emphasis",
        exercises = listOf(
            ExerciseSpec(
                "squats-weekend", "Squats or Leg Press", 3, "8–10", "Barbell or Leg Press",
                "Reinforce leg work from Thursday",
                "Goblet Squat", "12–15", "Deep range of motion",
                "Standard squat pattern, focus on depth and control"
            ),
            ExerciseSpec(
                "incline-db-press", "Incline Dumbbell Press", 3, "10–12", "Dumbbells + Incline Bench",
                "Bench at 30–45°, good stretch at bottom",
                "Incline Press-Ups", "15", "Feet elevated",
                "Press dumbbells up, lower with control, feel chest stretch"
            ),
            ExerciseSpec(
                "seated-cable-row", "Seated Cable Row", 3, "10–12", "Cable Machine",
                "Pull to lower chest, squeeze back, control the negative",
                "Dumbbell Rows", "12 each arm", "One arm at a time",
                "Sit tall, pull handle to lower chest, squeeze shoulder blades"
            ),
            ExerciseSpec(
                "shoulder-press", "Shoulder Press", 3, "10", "Dumbbells or Barbell",
                "Strict form, no leg drive",
                "Pike Press-Ups", "10–12", "Hips high in air",
                "Press overhead, lower to shoulders, keep core tight"
            ),
            ExerciseSpec(
                "hammer-curls", "Hammer Curls", 2, "12", "Dumbbells",
                "Neutral grip, no swinging",
                "Resistance Band Curls", "15", "Slow tempo",
                "Thumbs up grip, curl to shoulders, control the negative"
            ),
            ExerciseSpec(
                "tricep-pushdowns", "Tricep Pushdowns", 2, "12", "Cable Machine",
                "Elbows pinned to sides, squeeze at bottom",
                "Diamond Press-Ups", "10–12", "Hands together under chest",
                "Push down to full extension, squeeze triceps, control return"
            ),
            ExerciseSpec(
                "core-finisher", "Hanging Leg Raises or Cable Crunches", 3, "12", "Pull-up Bar or Cable",
                "Control the movement, feel abs working",
                "Dead Bugs", "15 each side", "Slower tempo",
                "Raise legs / crunch down, pause at contraction, lower slowly"
            ),
        )
    )

    val all = listOf(tuesday, thursday, weekendHome, weekendGym)

    fun byKey(key: String): WorkoutSpec = all.firstOrNull { it.key == key } ?: tuesday

    /** Weekly plan, mirroring the original schedule. */
    val weeklySchedule = listOf(
        DayPlan("Monday", "Boxing Class", "🥊", "45–60 mins", "High", DayKind.CLASS),
        DayPlan("Tuesday", "Home Lower Body", "🏠", "15–20 mins", "Moderate", DayKind.WORKOUT, "tuesday"),
        DayPlan("Wednesday", "Upper Body Lift Class", "🏋️", "45–60 mins", "High", DayKind.CLASS),
        DayPlan("Thursday", "GYM LEG DAY ⭐", "🦵", "45–55 mins", "High", DayKind.WORKOUT, "thursday", priority = true),
        DayPlan("Friday", "Rest / Optional", "😴", "—", "Low", DayKind.REST),
        DayPlan("Saturday", "Home or Gym Session", "💪", "20–50 mins", "Varies", DayKind.WORKOUT, "weekendHome"),
        DayPlan("Sunday", "Home or Gym Session", "💪", "20–50 mins", "Varies", DayKind.WORKOUT, "weekendHome"),
    )

    fun planFor(day: DayOfWeek): DayPlan = when (day) {
        DayOfWeek.MONDAY -> weeklySchedule[0]
        DayOfWeek.TUESDAY -> weeklySchedule[1]
        DayOfWeek.WEDNESDAY -> weeklySchedule[2]
        DayOfWeek.THURSDAY -> weeklySchedule[3]
        DayOfWeek.FRIDAY -> weeklySchedule[4]
        DayOfWeek.SATURDAY -> weeklySchedule[5]
        DayOfWeek.SUNDAY -> weeklySchedule[6]
    }

    fun isWorkoutDay(day: DayOfWeek): Boolean = planFor(day).kind != DayKind.REST
}
