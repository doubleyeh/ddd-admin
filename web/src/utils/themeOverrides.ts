import { type GlobalThemeOverrides } from 'naive-ui'

const lightThemeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#2080f0',
    primaryColorHover: '#409eff',
    primaryColorPressed: '#1060c0'
  },
  Layout: {
    siderColor: '#ffffff',
    headerColor: '#ffffff',
    color: '#f5f7f9'
  },
  Result: {
    titleTextColor: '#000',
    descriptionTextColor: '#000'
  }
}

const darkThemeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#409eff',
    primaryColorHover: '#66b1ff',
    primaryColorPressed: '#3a7bd5'
  },
  Layout: {
    siderColor: 'rgb(24, 24, 28)',
    headerColor: 'rgb(24, 24, 28)',
    color: 'rgb(16, 16, 20)',
    headerBorderColor: 'rgb(60, 60, 60)',
    siderBorderColor: 'rgb(60, 60, 60)'
  },
  Menu: {
    itemColorActive: 'rgba(255, 255, 255, 0.1)'
  },
  Result: {
    titleTextColor: '#fff',
    descriptionTextColor: '#fff'
  }
}

export { lightThemeOverrides, darkThemeOverrides }
