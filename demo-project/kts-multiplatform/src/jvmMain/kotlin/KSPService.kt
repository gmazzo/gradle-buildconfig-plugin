package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import com.google.auto.service.AutoService

interface KSPService

@AutoService(KSPService::class)
class KSPServiceImpl : KSPService
