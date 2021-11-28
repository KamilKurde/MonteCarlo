import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.*

class MonteCarloWorker(private val r: Double, scope: CoroutineScope, private val repeats: ULong) {
	var counter = 0UL
	var inCircle = 0UL

	var active by mutableStateOf(true)

	private fun randomDouble() = Random.nextDouble(-r, r)

	private suspend fun calc() {
		for (i in 0UL until repeats) {
			val x = randomDouble()
			val y = randomDouble()
			if (sqrt((x * x) + (y * y)) <= r) {
				inCircle++
				if (!coroutineContext.isActive)
					break
			}
			counter++
		}
		active = false
	}

	init {
		scope.launch { calc() }
	}
}

val icon @Composable get() = painterResource("icon.ico")

@OptIn(ExperimentalTime::class)
fun main(vararg args: String) = application {
	var counter by mutableStateOf(0UL)
	var inCircle by mutableStateOf(0UL)
	val desiredCounter = args.firstOrNull()?.toULong() ?: ULong.MAX_VALUE
	var totalTime = 0L
	val workerScope = CoroutineScope(Job() + Dispatchers.Default)
	val uiScope = CoroutineScope(Job() + Dispatchers.Main)
	val threads = Runtime.getRuntime().availableProcessors()
	val repeatsPerThread = desiredCounter / threads.toUInt()
	val r = sqrt(Double.MAX_VALUE / 2)
	val workers = Array(threads) { MonteCarloWorker(r, workerScope, repeatsPerThread) } + MonteCarloWorker(r, uiScope, desiredCounter - (threads.toUInt() * repeatsPerThread))
	Window(
		onCloseRequest = {
			workerScope.cancel()
			exitApplication()
		},
		resizable = false,
		state = rememberWindowState(size = DpSize(320.dp, 224.dp)),
		icon = icon,
		title = "Monte Carlo PI Calculator"
	) {
		Row(modifier = Modifier.fillMaxSize().padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
			Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(0.5f)) {
				Text("stdlib PI:")
				Text("Calculated pi:")
				Text("Points to calculate:")
				Text("Calculated points:")
				Text("In percent:")
				Text("Time:")
				Text("Available threads:")
				Text("Active workers:")
				Text("Speed:")
			}
			Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(0.5f)) {
				Text(Math.PI.toString())
				val pi = 4.0 / counter.toDouble() * inCircle.toDouble()
				Text(pi.toString())
				val percent = (counter.toDouble() * 100 / desiredCounter.toDouble()).toInt()
				Text(desiredCounter.toString())
				Text(counter.toString())
				Text("$percent %")
				Text(totalTime.toDuration(DurationUnit.MILLISECONDS).toString())
				Text(threads.toString())
				Text(workers.count { it.active }.toString())
				Text("${(counter.toDouble() / totalTime).roundToInt()} KOPS")
			}
		}
	}
	uiScope.launch {
		var lastCalc = System.currentTimeMillis()
		val delay = 33L
		while (isActive) {
			counter = workers.sumOf { it.counter }
			inCircle = workers.sumOf { it.inCircle }
			totalTime += System.currentTimeMillis() - lastCalc
			lastCalc = System.currentTimeMillis()
			if (counter == desiredCounter)
				break
			delay(delay)
		}
	}
}