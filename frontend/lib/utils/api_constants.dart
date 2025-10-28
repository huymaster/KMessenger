class ApiConstants {
  // Base URL cho backend API
  static const String baseUrl = 'http://127.0.0.1:8080/api/v1';
  
  // Timeout cho HTTP requests
  static const int timeoutDuration = 30000; // 30 seconds
  
  // API Endpoints
  static const String authRegister = '/auth/register';
  static const String authLogin = '/auth/login';
  static const String authRefresh = '/auth/refresh';
  static const String authLogout = '/auth/logout';
  static const String healthCheck = '/health';
  
  // Headers
  static const Map<String, String> headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };
  
  // Storage keys
  static const String accessTokenKey = 'access_token';
  static const String refreshTokenKey = 'refresh_token';
  static const String userIdKey = 'user_id';
}