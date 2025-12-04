package com.example.myfirstkotlinapp.exercise

import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.sqrt
import kotlin.math.acos
import kotlin.math.PI

object ExerciseLogic {


    private var isDown = false
    private var lowFrameCount = 0
    private var highFrameCount = 0
    private var lastCountTime = 0L
    private const val frameThreshold = 3
    private const val Cooldown = 500L
    private var Count = 0

    // ===== [추가: Deadlift 전용 상태] =========================================
    private var ddl_isDown = false
    private var ddl_lowFrames = 0
    private var ddl_highFrames = 0
    private var ddl_lastTime = 0L

    // Deadlift 스무딩/사이클 추적용 상태 (전역)
    private var ddl_smoothInit = false
    private var ddl_smoothDelta = 0f
    private var ddl_cycleMax = Float.NEGATIVE_INFINITY
    private var ddl_cycleMin = Float.POSITIVE_INFINITY

    // ===== [추가: Crunch 전용 상태] ===========================================
    private var cr_isClosed = false
    private var cr_lowFrames = 0
    private var cr_highFrames = 0
    private var cr_lastTime = 0L

    // ===== [추가: Elbow-to-Knee 전용 상태] ===================================
    private var e2k2_state = 0    // 0:첫쪽 NEAR, 1:첫쪽 FAR, 2:반대 NEAR, 3:반대 FAR
    private var e2k2_side = 0    // 0=(L elbow - R knee), 1=(R elbow - L knee)
    private var e2k2_init = false
    private var e2k2_lr = 0.0
    private var e2k2_rl = 0.0
    private var e2k2_lastTime = 0L

    // ---- Pike Push-up 전용 ----
    private var pike_isDown = false
    private var pike_lowFrames = 0
    private var pike_highFrames = 0
    private var pike_lastTime = 0L
    private var pike_smoothInit = false
    private var pike_vSmooth = 0f

    // 기존에 있던 스무딩 상태 유지
    private var cr_smoothInit = false
    private var cr_dSmooth = 0.0


    private fun distance2D(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val dx = (x1 - x2).toDouble()
        val dy = (y1 - y2).toDouble()
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }


    fun countSquat(result: PoseLandmarkerResult): Boolean {
        val landmarks = result.landmarks().firstOrNull() ?: return false

        val leftHipY = landmarks[23].y()
        val rightHipY = landmarks[24].y()
        val hipY = (leftHipY + rightHipY) / 2

        val leftKneeY = landmarks[25].y()
        val rightKneeY = landmarks[26].y()
        val kneeY = (leftKneeY + rightKneeY) / 2

        val currentTime = System.currentTimeMillis()

        val hipBelowKnee = hipY > kneeY + 0.02f
        val hipAboveKnee = hipY < kneeY - 0.02f

        if (hipBelowKnee) {
            lowFrameCount++
            highFrameCount = 0
            if (lowFrameCount >= 3) {
                isDown = true
            }
        } else if (hipAboveKnee && isDown) {
            highFrameCount++
            if (highFrameCount >= 3 && currentTime - lastCountTime > Cooldown) {
                lastCountTime = currentTime
                lowFrameCount = 0
                highFrameCount = 0
                isDown = false
                Count++
                return true
            }
        } else {
            lowFrameCount = 0
            highFrameCount = 0
        }

        return false
    }


    fun countPushup(result: PoseLandmarkerResult): Boolean {
        val landmarks = result.landmarks().firstOrNull() ?: return false

        val leftShoulderY = landmarks[11].y()
        val rightShoulderY = landmarks[12].y()
        val shoulderY = (leftShoulderY + rightShoulderY) / 2

        val leftElbowY = landmarks[13].y()
        val rightElbowY = landmarks[14].y()
        val elbowY = (leftElbowY + rightElbowY) / 2

        val currentTime = System.currentTimeMillis()

        // 다운: 어깨와 팔꿈치 y좌표가 거의 같을 때 (팔꿈치가 어깨 옆에 온 상태)
        val downPose = Math.abs(shoulderY - elbowY) < 0.02f

        // 업: 어깨가 팔꿈치보다 충분히 위에 있을 때 (팔을 편 상태)
        val upPose = shoulderY < elbowY - 0.025f

        if (downPose) {
            lowFrameCount++
            highFrameCount = 0
            if (lowFrameCount >= 3) {
                isDown = true
            }
        } else if (upPose && isDown) {
            highFrameCount++
            if (highFrameCount >= 3 && currentTime - lastCountTime > Cooldown) {
                lastCountTime = currentTime
                lowFrameCount = 0
                highFrameCount = 0
                isDown = false
                Count++
                return true
            }
        } else {
            lowFrameCount = 0
            highFrameCount = 0
        }

        return false
    }

    fun countPullup(result: PoseLandmarkerResult): Boolean {
        val landmarks = result.landmarks().firstOrNull() ?: return false

        val leftWristY = landmarks[15].y()
        val rightWristY = landmarks[16].y()
        val wristY = (leftWristY + rightWristY) / 2

        val leftShoulderY = landmarks[11].y()
        val rightShoulderY = landmarks[12].y()
        val shoulderY = (leftShoulderY + rightShoulderY) / 2

        val currentTime = System.currentTimeMillis()
        val shoulderToWristDist = shoulderY - wristY

        // 기준값은 실제 실험하며 조정 필요 (예: 0.07f, 0.13f)
        val upPose = shoulderToWristDist < 0.07f
        val downPose = shoulderToWristDist > 0.13f

        if (upPose) {
            lowFrameCount++
            highFrameCount = 0
            if (lowFrameCount >= 3) {
                isDown = true
            }
        } else if (downPose && isDown) {
            highFrameCount++
            if (highFrameCount >= 3 && currentTime - lastCountTime > Cooldown) {
                lastCountTime = currentTime
                lowFrameCount = 0
                highFrameCount = 0
                isDown = false
                Count++
                return true
            }
        } else {
            lowFrameCount = 0
            highFrameCount = 0
        }

        return false
    }

    fun countShoulderPress(result: PoseLandmarkerResult): Boolean {
        val landmarks = result.landmarks().firstOrNull() ?: return false
        val currentTime = System.currentTimeMillis()

        val leftShoulderY = landmarks[11].y()
        val rightShoulderY = landmarks[12].y()
        val shoulderY = (leftShoulderY + rightShoulderY) / 2

        val leftWristY = landmarks[15].y()
        val rightWristY = landmarks[16].y()
        val wristY = (leftWristY + rightWristY) / 2

        val leftElbowY = landmarks[13].y()
        val rightElbowY = landmarks[14].y()
        val elbowY = (leftElbowY + rightElbowY) / 2

        val isPushedUp = wristY < shoulderY - 0.02f && elbowY < shoulderY - 0.01f
        val isLowered = elbowY > shoulderY + 0.03f

        if (isLowered && !isDown) {
            isDown = true
        }

        if (isPushedUp && isDown && currentTime - lastCountTime > 400L) {
            isDown = false
            lastCountTime = currentTime
            return true
        }

        return false
    }

    fun countLegRaise(result: PoseLandmarkerResult): Boolean {
        val landmarks = result.landmarks().firstOrNull() ?: return false

        // 엉덩이, 무릎, 발목 좌표
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]
        val leftKnee = landmarks[25]
        val rightKnee = landmarks[26]
        val leftAnkle = landmarks[27]
        val rightAnkle = landmarks[28]

        // 양쪽 다리의 각도 계산 (엉덩이-무릎-발목 각도)
        val leftLegAngle = calculateAngle(
            leftHip.x(), leftHip.y(),
            leftKnee.x(), leftKnee.y(),
            leftAnkle.x(), leftAnkle.y()
        )
        val rightLegAngle = calculateAngle(
            rightHip.x(), rightHip.y(),
            rightKnee.x(), rightKnee.y(),
            rightAnkle.x(), rightAnkle.y()
        )

        // 평균 다리 각도
        val avgLegAngle = (leftLegAngle + rightLegAngle) / 2

        val currentTime = System.currentTimeMillis()

        // 다리가 올라간 상태 (각도가 작을 때 - 다리가 몸통에 가까워짐)
        val legsUp = avgLegAngle < 100.0
        // 다리가 내려간 상태 (각도가 클 때)
        val legsDown = avgLegAngle > 140.0

        if (legsUp) {
            lowFrameCount++
            highFrameCount = 0
            if (lowFrameCount >= 5) {
                isDown = true // 다리 올린 상태를 low로 간주.
            }
        } else if (legsDown && isDown) {
            highFrameCount++
            if (highFrameCount >= 5 && currentTime - lastCountTime > 800L) {
                lastCountTime = currentTime
                highFrameCount = 0
                lowFrameCount = 0
                isDown = false
                Count++
                return true
            }
        } else {
            if (highFrameCount > 0) highFrameCount--
            if (lowFrameCount > 0) lowFrameCount--
        }

        return false
    }

    // 세 점으로 각도를 계산하는 함수
    private fun calculateAngle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Double {
        val vector1X = x1 - x2
        val vector1Y = y1 - y2
        val vector2X = x3 - x2
        val vector2Y = y3 - y2

        val dot = vector1X * vector2X + vector1Y * vector2Y
        val magnitude1 = sqrt((vector1X * vector1X + vector1Y * vector1Y).toDouble())
        val magnitude2 = sqrt((vector2X * vector2X + vector2Y * vector2Y).toDouble())

        val cosAngle = dot / (magnitude1 * magnitude2)
        val angle = acos(cosAngle.coerceIn(-1.0, 1.0)) * 180.0 / PI

        return angle
    }
    fun countDumbbellDeadlift(result: PoseLandmarkerResult): Boolean {
        val lm = result.landmarks().firstOrNull() ?: return false

        val lHip = lm[23]; val rHip = lm[24]
        val lAnk = lm[27]; val rAnk = lm[28]
        val lWr  = lm[15]; val rWr  = lm[16]

        // NaN/이상치 방어
        val vals = floatArrayOf(lHip.y(), rHip.y(), lAnk.y(), rAnk.y(), lWr.y(), rWr.y())
        if (vals.any { it.isNaN() }) return false

        val hipY = (lHip.y() + rHip.y()) / 2f
        val ankY = (lAnk.y() + rAnk.y()) / 2f
        val wristY = (lWr.y() + rWr.y()) / 2f

        // Δy(손목-골반): 내려가면 커짐, 올라오면 작아짐
        val rawDelta = wristY - hipY

        // EMA 스무딩
        if (!ddl_smoothInit) {
            ddl_smoothDelta = rawDelta
            ddl_smoothInit = true
        } else {
            val alpha = 0.4f
            ddl_smoothDelta = alpha * rawDelta + (1f - alpha) * ddl_smoothDelta
        }

        // 신체 스케일(힙→발목 세로거리)
        val hipAnkle = (ankY - hipY).coerceAtLeast(0.05f)

        // 동적 임계값 (카메라 거리 보정)
        val downThr = hipAnkle * 0.40f
        val upThr   = hipAnkle * 0.18f
        val minROM  = hipAnkle * 0.18f

        // 사이클 내 피크/바닥 추적
        if (!ddl_isDown) {
            ddl_cycleMax = Float.NEGATIVE_INFINITY
            ddl_cycleMin = Float.POSITIVE_INFINITY
        } else {
            if (ddl_smoothDelta > ddl_cycleMax) ddl_cycleMax = ddl_smoothDelta
            if (ddl_smoothDelta < ddl_cycleMin) ddl_cycleMin = ddl_smoothDelta
        }

        val now = System.currentTimeMillis()

        // 1) 다운 진입
        if (!ddl_isDown) {
            if (ddl_smoothDelta > downThr) {
                ddl_lowFrames++
                if (ddl_lowFrames >= 3) {
                    ddl_isDown = true
                    ddl_highFrames = 0
                    ddl_cycleMax = ddl_smoothDelta
                    ddl_cycleMin = ddl_smoothDelta
                }
            } else if (ddl_lowFrames > 0) {
                ddl_lowFrames--
            }
            return false
        }

        // 2) 업 완료 → +1
        val reachedUp = ddl_smoothDelta < upThr
        val romNow = (ddl_cycleMax - ddl_smoothDelta)

        if (reachedUp && romNow > minROM) {
            ddl_highFrames++
            if (ddl_highFrames >= 3 && now - ddl_lastTime > Cooldown) {
                ddl_lastTime = now
                ddl_isDown = false
                ddl_lowFrames = 0
                ddl_highFrames = 0
                Count++
                return true
            }
        } else if (ddl_highFrames > 0) {
            ddl_highFrames--
        }
        return false
    }

    fun countCrunchFloor(result: PoseLandmarkerResult): Boolean {
        val lm = result.landmarks().firstOrNull() ?: return false

        val lSh = lm[11]; val rSh = lm[12]
        val lEl = lm[13]; val rEl = lm[14]
        val lKn = lm[25]; val rKn = lm[26]

        // NaN 방어
        val vals = listOf(lSh, rSh, lEl, rEl, lKn, rKn)
            .flatMap { listOf(it.x(), it.y()) }
        if (vals.any { it.isNaN() }) return false

        val shoulderDist = distance2D(lSh.x(), lSh.y(), rSh.x(), rSh.y())

        val dL = distance2D(lEl.x(), lEl.y(), lKn.x(), lKn.y())
        val dR = distance2D(rEl.x(), rEl.y(), rKn.x(), rKn.y())
        val dAvg = (dL + dR) / 2.0
        val dMin = kotlin.math.min(dL, dR)

        // EMA
        if (!cr_smoothInit) {
            cr_dSmooth = dAvg
            cr_smoothInit = true
        } else {
            val alpha = 0.5
            cr_dSmooth = alpha * dAvg + (1 - alpha) * cr_dSmooth
        }

        // 임계(어깨폭 기반)
        val closeThrAvg = (shoulderDist * 0.78).coerceIn(0.145, 0.36)
        val closeThrMin = (shoulderDist * 0.43).coerceIn(0.105, 0.27)
        val farMarginAvg = (shoulderDist * 0.045).coerceIn(0.011, 0.048)
        val farThrAvg = closeThrAvg + farMarginAvg

        val now = System.currentTimeMillis()

        val isCloseNow = (cr_dSmooth < closeThrAvg) || (dMin < closeThrMin)
        val isFarNow = (cr_dSmooth > farThrAvg)

        // 닫힘(근접) 진입
        if (!cr_isClosed) {
            if (isCloseNow) {
                cr_lowFrames++
                cr_highFrames = 0
                if (cr_lowFrames >= 2) cr_isClosed = true
            } else if (cr_lowFrames > 0) {
                cr_lowFrames--
            }
            return false
        }

        // 열림(이탈) → +1
        if (isFarNow) {
            cr_highFrames++
            if (cr_highFrames >= 2 && now - cr_lastTime > Cooldown) {
                cr_lastTime = now
                cr_isClosed = false
                cr_lowFrames = 0
                cr_highFrames = 0
                Count++
                return true
            }
        } else if (cr_highFrames > 0) {
            cr_highFrames--
        }
        return false
    }

    fun countElbowToKnee(result: PoseLandmarkerResult): Boolean {
        val lm = result.landmarks().firstOrNull() ?: return false
        val lEl = lm[13]; val rEl = lm[14]
        val lKn = lm[25]; val rKn = lm[26]
        val lSh = lm[11]; val rSh = lm[12]

        // NaN 방어
        val vals = listOf(lEl, rEl, lKn, rKn, lSh, rSh)
            .flatMap { listOf(it.x(), it.y()) }
        if (vals.any { it.isNaN() }) return false

        val dLR_raw = distance2D(lEl.x(), lEl.y(), rKn.x(), rKn.y())
        val dRL_raw = distance2D(rEl.x(), rEl.y(), lKn.x(), lKn.y())

        if (!e2k2_init) {
            e2k2_lr = dLR_raw
            e2k2_rl = dRL_raw
            e2k2_init = true
        } else {
            val alpha = 0.68
            e2k2_lr = alpha * dLR_raw + (1 - alpha) * e2k2_lr
            e2k2_rl = alpha * dRL_raw + (1 - alpha) * e2k2_rl
        }

        val shoulderDist = distance2D(lSh.x(), lSh.y(), rSh.x(), rSh.y())
        val nearThr = (shoulderDist * 0.85).coerceIn(0.14, 0.34)
        val farThr  = nearThr + (shoulderDist * 0.035).coerceIn(0.010, 0.035)

        fun isNear(side: Int) = if (side == 0) e2k2_lr < nearThr else e2k2_rl < nearThr
        fun isFar (side: Int) = if (side == 0) e2k2_lr > farThr  else e2k2_rl > farThr

        val now = System.currentTimeMillis()

        when (e2k2_state) {
            0 -> { // 첫 근접 대기 — 동시에 가까우면 더 가까운 쪽
                val pick = if (e2k2_lr <= e2k2_rl) 0 else 1
                if (isNear(pick)) {
                    e2k2_side = pick
                    e2k2_state = 1
                }
                return false
            }
            1 -> { // 선택 쪽 FAR 대기
                if (isFar(e2k2_side)) {
                    e2k2_side = 1 - e2k2_side
                    e2k2_state = 2
                }
                return false
            }
            2 -> { // 반대쪽 NEAR 대기
                if (isNear(e2k2_side)) {
                    e2k2_state = 3
                }
                return false
            }
            3 -> { // 반대쪽 FAR → +1
                if (isFar(e2k2_side)) {
                    if (now - e2k2_lastTime > Cooldown) {
                        e2k2_lastTime = now      // 전용 쿨다운
                        e2k2_state = 0
                        Count++                  // 총합 카운트는 그대로 사용
                        return true
                    }
                }
                return false
            }
        }
        return false
    }


    fun countPikePushup(result: PoseLandmarkerResult): Boolean {
        val lm = result.landmarks().firstOrNull() ?: return false

        // 필요한 랜드마크
        val nose = lm[0]
        val lEye = lm[2]; val rEye = lm[5]
        val lEar = lm[7]; val rEar = lm[8]
        val mouthL = lm[9]; val mouthR = lm[10]
        val lSh  = lm[11]; val rSh  = lm[12]
        val lWr  = lm[15]; val rWr  = lm[16]

        // NaN 가드
        val vals = listOf(nose, lEye, rEye, lEar, rEar, mouthL, mouthR, lSh, rSh, lWr, rWr)
            .flatMap { listOf(it.x(), it.y()) }
        if (vals.any { it.isNaN() || it.isInfinite() }) return false

        // 스케일(사람 크기 보정)
        val shoulderWidth = distance2D(lSh.x(), lSh.y(), rSh.x(), rSh.y()).toFloat().coerceAtLeast(0.05f)

        // 손목 높이(바닥 기준)
        val wristY = (lWr.y() + rWr.y()) / 2f

        // ✅ 머리의 "가장 낮은 지점" (정수리 포함되도록 머리 부위 중 Y 최댓값 사용)
        val headLowY = maxOf(
            nose.y(), lEye.y(), rEye.y(), lEar.y(), rEar.y(), mouthL.y(), mouthR.y()
        )

        // 핵심 신호: 머리(가장 낮은 점) ↔ 손목의 Y거리 (작을수록 바닥에 근접)
        val dyRaw: Float = kotlin.math.abs(headLowY - wristY)

        // EMA 스무딩
        if (!pike_smoothInit) {
            pike_vSmooth = dyRaw
            pike_smoothInit = true
        } else {
            val a = 0.5f
            pike_vSmooth = a * dyRaw + (1f - a) * pike_vSmooth
        }

        // 히스테리시스 임계값 (완화)
        // - 이전보다 downThr를 넉넉히 키워서 "얼굴이 닿기 전, 정수리 근접" 시점에도 잡히게
        val downThr = (0.18f * shoulderWidth).coerceIn(0.035f, 0.10f)   // 근접 인정
        val upThr   = (downThr + 0.08f * shoulderWidth).coerceIn(0.06f, 0.16f) // 이탈 인정

        val now = System.currentTimeMillis()

        // ↓ Down(머리 근접) 진입
        if (!pike_isDown) {
            if (pike_vSmooth < downThr) {
                if (++pike_lowFrames >= 3) {
                    pike_isDown = true
                    pike_highFrames = 0
                }
            } else if (pike_lowFrames > 0) {
                pike_lowFrames--
            }
            return false
        }

        // ↑ Up(머리 상승/이탈) → +1
        if (pike_vSmooth > upThr) {
            if (++pike_highFrames >= 3 && now - pike_lastTime > Cooldown) {
                pike_lastTime = now
                pike_isDown = false
                pike_lowFrames = 0
                pike_highFrames = 0
                Count++
                return true
            }
        } else if (pike_highFrames > 0) {
            pike_highFrames--
        }

        return false
    }


    fun resetCount() {
        Count = 0
        isDown = false
        lowFrameCount = 0
        highFrameCount = 0
        lastCountTime = 0L

        // ---- Deadlift 전용 ----
        ddl_isDown = false
        ddl_lowFrames = 0
        ddl_highFrames = 0
        ddl_lastTime = 0L
        // 스무딩/사이클 추적(Deadlift용)
        ddl_smoothInit = false
        ddl_smoothDelta = 0f
        ddl_cycleMax = Float.NEGATIVE_INFINITY
        ddl_cycleMin = Float.POSITIVE_INFINITY

        // ---- Crunch 전용 ----
        cr_isClosed = false
        cr_lowFrames = 0
        cr_highFrames = 0
        cr_lastTime = 0L
        cr_smoothInit = false
        cr_dSmooth = 0.0

        // ---- Elbow-to-Knee 전용 ----
        e2k2_state = 0
        e2k2_side = 0
        e2k2_init = false
        e2k2_lr = 0.0
        e2k2_rl = 0.0
        e2k2_lastTime = 0L

        // ---- Pike Push-up 전용 ----
        pike_isDown = false
        pike_lowFrames = 0
        pike_highFrames = 0
        pike_lastTime = 0L
        pike_smoothInit = false
        pike_vSmooth = 0f
    }
}