package com.github.gmazzo.buildconfig.demos.kmp

import com.google.auto.service.AutoService

interface KSPService

@AutoService(KSPService::class)
class KSPServiceImpl : KSPService
