cleari()
size(64, 64)
setBackground(noColor)

val grad = cm.linearGradient(0, 0, cm.green, 60, 60, ColorMaker.hsl(110, 0.70, 0.60))

val rect = Picture {
    repeat(6) {
        forward(40)
        right(360.0 / 5)
    }
}.withFillColor(grad).withNoPen()
drawCentered(rect)