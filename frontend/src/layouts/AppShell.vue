<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">LP</span>
        <span class="brand-name">LifePilot</span>
      </div>
      <nav class="nav-list" aria-label="Primary">
        <span class="nav-item active">总览</span>
        <span class="nav-item">记账</span>
        <span class="nav-item">购物</span>
        <span class="nav-item">库存</span>
        <span class="nav-item">待办</span>
      </nav>
    </aside>
    <main class="content">
      <slot />
    </main>
  </div>
</template>
