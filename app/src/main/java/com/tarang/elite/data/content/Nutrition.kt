package com.tarang.elite.data.content

data class MacroTargets(val calories: String, val protein: String, val carbs: String, val fat: String, val proteinGrams: Int)

data class Meal(val time: String, val name: String, val items: String, val protein: String, val notes: String)

data class FoodItem(val name: String, val portion: String, val protein: String?, val notes: String)

data class AvoidFood(val name: String, val reason: String)

object NutritionLibrary {

    val workoutDayTargets = MacroTargets("2100–2300", "130–150g", "200–250g", "65–80g", 150)
    val restDayTargets = MacroTargets("1900–2100", "120–130g", "150–180g", "60–75g", 130)

    val workoutDayMeals = listOf(
        Meal("7:30am", "Breakfast", "3 boiled eggs + 1 slice seeded toast + 150g Greek yoghurt with berries", "36g", "This fuels your morning"),
        Meal("10:30am", "Snack (optional)", "Small handful almonds + banana", "5g", "If hungry between meals"),
        Meal("1:00pm", "Lunch", "Huel pasta + 100g paneer cubes + side of spinach", "45g", "Essential protein boost"),
        Meal("6:00pm", "Pre-Workout", "Banana + small handful of nuts", "3g", "30 mins before gym"),
        Meal("Post-Workout", "Post-Workout Shake", "1 scoop protein powder with water", "25g", "Within 30 mins of training"),
        Meal("8:30pm", "Dinner", "Chana masala + 100g paneer tikka + 1 roti + cucumber raita", "40g", "Protein-rich Indian feast"),
    )

    val restDayMeals = listOf(
        Meal("7:30am", "Breakfast", "2 eggs scrambled on 1 toast + 150g Greek yoghurt", "30g", "Slightly smaller without workout"),
        Meal("1:00pm", "Lunch", "Huel pasta + 150g firm tofu (pan-fried with soy sauce)", "43g", "Tofu variation for variety"),
        Meal("3:30pm", "Snack", "Cottage cheese (150g) with cucumber/carrot sticks", "18g", "Keeps protein high between meals"),
        Meal("8:00pm", "Dinner", "Dal tadka + paneer bhurji + 1 roti", "38g", "Traditional with protein boost"),
    )

    val goodFoods = listOf(
        FoodItem("Eggs", "3 whole", "21g", "Have daily"),
        FoodItem("Paneer", "100g (deck of cards)", "25g", "Your secret weapon"),
        FoodItem("Tofu (firm)", "150g", "18g", "Marinate for taste"),
        FoodItem("Greek Yoghurt (full-fat)", "170g pot", "15–17g", "Daily staple"),
        FoodItem("Cottage Cheese", "150g", "18g", "Great snack"),
        FoodItem("Lentils/Dal", "200g cooked", "12–14g", "Good base, add paneer"),
        FoodItem("Chickpeas", "150g cooked", "11g", "Chana masala"),
        FoodItem("Kidney Beans (Rajma)", "150g cooked", "12g", "Excellent with rice"),
        FoodItem("Edamame", "100g", "11g", "Great snack"),
        FoodItem("Quinoa", "150g cooked", "8g", "Rice alternative"),
        FoodItem("Whey/Plant Protein", "1 scoop (30g)", "24–25g", "Post-workout"),
    )

    val okayFoods = listOf(
        FoodItem("Rice", "150g cooked (fist-sized)", null, "Post-workout mainly"),
        FoodItem("Chapati/Roti", "2 medium", null, "Watch portions"),
        FoodItem("Wholemeal Bread", "2 slices", null, "OK but not high protein"),
        FoodItem("Potato/Sweet Potato", "150g", null, "Good around training"),
        FoodItem("Milk", "250ml", null, "8g protein"),
        FoodItem("Nuts/Nut Butter", "30g (small handful)", null, "Calorie-dense"),
        FoodItem("Oats", "50g dry", null, "Good breakfast base"),
    )

    val avoidFoods = listOf(
        AvoidFood("Mass Gainer Shakes", "Too many carbs/calories for your goals"),
        AvoidFood("White Bread, Naan", "Low nutrition, spikes blood sugar"),
        AvoidFood("Fried Foods (pakoras, samosas)", "Calorie bombs, poor nutrition"),
        AvoidFood("Sugary Drinks, Fruit Juice", "Empty calories"),
        AvoidFood("Biscuits, Cakes, Mithai", "Save for occasional treats only"),
        AvoidFood("Crisps", "Easy to overeat, low nutrition"),
        AvoidFood("Alcohol", "Kills muscle protein synthesis"),
        AvoidFood("Creamy Curries (korma, butter)", "High calorie — choose tomato-based"),
        AvoidFood("Excessive Ghee/Oil", "Hidden 200–300 calories"),
    )
}
