package com.tarang.elite.data.content

data class Quote(val text: String, val author: String)
data class Story(val title: String, val content: String)
data class MeditationGuide(val title: String, val duration: String, val time: String, val steps: List<String>)
data class HealthTip(val title: String, val emoji: String, val target: String, val tips: List<String>)

object MindContent {

    val trainerWorkout = listOf(
        "💪 Time to build that body, Tarang. Champions are made when no one's watching.",
        "🔥 Those legs won't build themselves. Let's get after it!",
        "⚡ Remember: every rep is a vote for the person you're becoming.",
        "🏆 48 is just a number. Your potential? Unlimited.",
        "💎 Pain is temporary. The pride of transformation lasts forever.",
        "🎯 Focus. Execute. Dominate. That's the formula.",
    )

    val trainerRest = listOf(
        "🧘 Recovery is where growth happens. Honour your rest.",
        "😴 Your muscles are rebuilding right now. Sleep like a champion tonight.",
        "🌱 Even elite athletes know: rest days build winners.",
    )

    val trainerMotivation = listOf(
        "You're not just building muscle — you're building the father your kids will be proud of.",
        "Every set, every rep, every meal — you're rewriting your story.",
        "The man in the mirror 3 months from now is counting on today's effort.",
        "Discipline beats motivation. Show up anyway.",
    )

    val trainerCompleted = listOf(
        "🎉 SESSION COMPLETE! That's how champions operate.",
        "✅ Another one in the bank. Consistency compounds.",
        "🔥 Crushed it! Your future self thanks you.",
    )

    val quotes = listOf(
        Quote("The only bad workout is the one that didn't happen.", "Unknown"),
        Quote("Success isn't always about greatness. It's about consistency.", "Dwayne Johnson"),
        Quote("The pain you feel today will be the strength you feel tomorrow.", "Arnold Schwarzenegger"),
        Quote("Your body can stand almost anything. It's your mind you have to convince.", "Unknown"),
        Quote("The difference between try and triumph is just a little umph!", "Marvin Phillips"),
        Quote("Strength does not come from physical capacity. It comes from an indomitable will.", "Mahatma Gandhi"),
        Quote("The clock is ticking. Are you becoming the person you want to be?", "Greg Plitt"),
        Quote("Fall seven times, stand up eight.", "Japanese Proverb"),
    )

    val stories = listOf(
        Story(
            "The Bamboo Tree",
            "A farmer plants a bamboo seed and waters it every day. For 5 years, nothing visible happens. Then, in just 6 weeks, the bamboo grows 90 feet. Was it growing those 5 years? Absolutely — building roots deep underground. Your fitness journey is the same. Trust the process, Tarang. The roots are growing."
        ),
        Story(
            "The Two Wolves",
            "An old Cherokee told his grandson: 'Inside me, two wolves fight. One is fear, doubt, and laziness. The other is discipline, strength, and courage.' The boy asked, 'Which wolf wins?' The grandfather replied: 'The one you feed.' Every workout, every healthy meal — you're feeding your strongest wolf."
        ),
        Story(
            "Bruce Lee's 10,000 Kicks",
            "'I fear not the man who has practiced 10,000 kicks once, but I fear the man who has practiced one kick 10,000 times.' You don't need a perfect plan. You need to execute your plan perfectly, over and over. Master the basics: squat, press, row, eat protein. That's your 10,000 kicks."
        ),
        Story(
            "The Sculptor's Approach",
            "Michelangelo was asked how he created David. He said: 'I simply removed everything that wasn't David.' Your buffed physique is already inside you, covered by what needs to be chipped away. Each workout reveals more of who you're meant to be."
        ),
    )

    val morningMeditation = MeditationGuide(
        "Morning Meditation", "5–10 minutes", "Before breakfast",
        listOf(
            "Sit comfortably, spine straight",
            "Close eyes, take 3 deep breaths",
            "Set intention: 'Today I build the best version of myself'",
            "Focus on breath for 5 minutes",
            "Visualise yourself completing today's workout successfully",
            "Open eyes, start your day with purpose",
        )
    )

    val eveningMeditation = MeditationGuide(
        "Evening Meditation", "5–10 minutes", "Before bed (after kids sleep)",
        listOf(
            "Lie down or sit comfortably",
            "Release tension from feet to head",
            "Reflect on 3 things you did well today",
            "Acknowledge any challenges without judgment",
            "Breathe deeply, counting exhales from 10 to 1",
            "Let go of the day, prepare for restorative sleep",
        )
    )

    val visioningSteps = listOf(
        "Close eyes and see yourself 3 months from now",
        "Notice your shoulders — broader, more defined",
        "See your arms filling out your shirt sleeves",
        "Look at your legs — powerful, athletic",
        "Feel the confidence when you look in the mirror",
        "Imagine your kids looking up at their strong father",
        "Hold this image. This is who you're becoming.",
        "Write down one thing you'll do this week to get closer",
    )

    val napBenefits = listOf(
        "Enhances muscle recovery",
        "Boosts afternoon energy",
        "Improves cognitive function",
        "Reduces cortisol (stress hormone)",
    )

    val napTips = listOf(
        "Set alarm for 20 minutes max",
        "Dark, cool room",
        "Don't nap after 3pm (disrupts night sleep)",
        "Nap-a-latte: drink a quick coffee, then nap 20–25 mins — wake as the caffeine kicks in",
    )

    val healthTips = listOf(
        HealthTip(
            "Hydration", "💧", "2.5–3 litres daily",
            listOf(
                "Start day with 500ml water before breakfast",
                "Keep water bottle visible at desk",
                "Drink 500ml during each workout",
                "Urine should be pale yellow",
                "Add electrolytes on heavy training days",
                "Herbal tea counts toward total",
            )
        ),
        HealthTip(
            "Blood Sugar Control", "📊", "Stable energy all day",
            listOf(
                "Protein at every meal stabilises blood sugar",
                "Avoid sugary drinks and fruit juice",
                "Pair carbs with protein or fat",
                "Walk for 10 mins after large meals",
                "Save sweets for post-workout if at all",
                "Cinnamon may help insulin sensitivity",
            )
        ),
        HealthTip(
            "Blood Pressure", "❤️", "Under 120/80 mmHg",
            listOf(
                "Regular exercise naturally lowers BP",
                "Limit sodium (watch processed foods)",
                "Potassium-rich foods help (bananas, spinach)",
                "Manage stress through meditation",
                "Limit alcohol",
                "Get enough sleep (7–9 hours)",
            )
        ),
        HealthTip(
            "Mind Control", "🧠", "Focused, resilient, growth-oriented",
            listOf(
                "Start day with intention, not phone",
                "Celebrate small wins daily",
                "Reframe 'I have to' as 'I get to'",
                "Fatigue is often mental — push through initial resistance",
                "Compare yourself only to yesterday's you",
                "Visualisation primes your brain for success",
            )
        ),
        HealthTip(
            "Sleep Protocol", "🌙", "7–9 hours, consistent",
            listOf(
                "Wait ~90 minutes after waking before caffeine",
                "On waking: 15 deep breaths, ~450ml water, 15 mins of daylight",
                "Stop eating and alcohol 3 hours before bed",
                "Keep the bedroom cool — a temperature drop triggers melatonin",
                "Wake at 3am? Don't check the clock — use 4-7-8 breathing instead",
                "Side sleeping (left side) is generally best for digestion",
            )
        ),
    )
}
