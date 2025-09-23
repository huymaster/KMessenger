package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity


interface BaseEntity<T : BaseEntity<T>> : Entity<T>