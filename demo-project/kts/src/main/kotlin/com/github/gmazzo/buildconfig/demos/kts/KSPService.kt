package com.github.gmazzo.buildconfig.demos.kts

import com.google.auto.service.AutoService

interface KSPService

@AutoService(KSPService::class)
class KSPServiceImpl : KSPService
