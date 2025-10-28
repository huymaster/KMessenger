import 'package:shared_preferences/shared_preferences.dart';
import '../models/auth_models.dart';
import '../models/api_response.dart';
import '../utils/api_constants.dart';
import 'http_service.dart';

class AuthService {
  static final AuthService _instance = AuthService._internal();
  factory AuthService() => _instance;
  AuthService._internal();

  final HttpService _httpService = HttpService();

  // Register new user
  Future<ApiResponse<String>> register(RegisterRequest request) async {
    final response = await _httpService.post<String>(
      ApiConstants.authRegister,
      body: request.toJson(),
      includeAuth: false,
    );

    if (response.success) {
      return ApiResponse.success(
        response.data ?? 'OK',
        message: response.message ?? 'Registration successful',
      );
    } else {
      return ApiResponse.error(response.error ?? 'Registration failed');
    }
  }

  // Login user
  Future<ApiResponse<AuthResponse>> login(LoginRequest request) async {
    final response = await _httpService.post<Map<String, dynamic>>(
      ApiConstants.authLogin,
      body: request.toJson(),
      includeAuth: false,
    );

    if (response.success && response.data != null) {
      try {
        // Backend returns only {refreshToken: "..."}
        final refreshToken = response.data!['refreshToken'] as String?;
        if (refreshToken == null) {
          return ApiResponse.error('Invalid response: missing refreshToken');
        }

        // Store refresh token temporarily
        final prefs = await SharedPreferences.getInstance();
        await prefs.setString(ApiConstants.refreshTokenKey, refreshToken);

        // Now call refresh to get access token
        final refreshResponse = await _httpService.get<Map<String, dynamic>>(
          '${ApiConstants.authRefresh}?refreshToken=$refreshToken',
          includeAuth: false,
        );

        if (refreshResponse.success && refreshResponse.data != null) {
          final accessToken = refreshResponse.data!['accessToken'] as String?;
          if (accessToken == null) {
            return ApiResponse.error('Invalid response: missing accessToken');
          }

          // Store access token
          await prefs.setString(ApiConstants.accessTokenKey, accessToken);

          // Return a minimal AuthResponse (we don't have user info from backend yet)
          final authResponse = AuthResponse(
            accessToken: accessToken,
            refreshToken: refreshToken,
            user: User(
              userId: '',
              phoneNumber: request.phoneNumber,
              username: '',
              displayName: '',
              createdAt: DateTime.now(),
            ),
          );

          return ApiResponse.success(authResponse, message: 'Login successful');
        } else {
          return ApiResponse.error(refreshResponse.error ?? 'Failed to get access token');
        }
      } catch (e) {
        return ApiResponse.error('Failed to parse auth response: $e');
      }
    } else {
      return ApiResponse.error(response.error ?? 'Login failed');
    }
  }

  // Refresh access token
  Future<ApiResponse<AuthResponse>> refreshToken() async {
    final prefs = await SharedPreferences.getInstance();
    final storedRefreshToken = prefs.getString(ApiConstants.refreshTokenKey);
    
    if (storedRefreshToken == null) {
      return ApiResponse.error('No refresh token found');
    }

    final response = await _httpService.get<Map<String, dynamic>>(
      '${ApiConstants.authRefresh}?refreshToken=$storedRefreshToken',
      includeAuth: false,
    );

    if (response.success && response.data != null) {
      try {
        // Backend returns only {accessToken: "..."}
        final accessToken = response.data!['accessToken'] as String?;
        if (accessToken == null) {
          return ApiResponse.error('Invalid response: missing accessToken');
        }
        
        // Update stored access token
        await prefs.setString(ApiConstants.accessTokenKey, accessToken);
        
        // Return minimal AuthResponse
        final authResponse = AuthResponse(
          accessToken: accessToken,
          refreshToken: storedRefreshToken,
          user: User(
            userId: '',
            phoneNumber: '',
            username: '',
            displayName: '',
            createdAt: DateTime.now(),
          ),
        );
        
        return ApiResponse.success(authResponse, message: 'Token refreshed');
      } catch (e) {
        return ApiResponse.error('Failed to parse refresh response: $e');
      }
    } else {
      return ApiResponse.error(response.error ?? 'Token refresh failed');
    }
  }

  // Logout user
  Future<ApiResponse<void>> logout() async {
    final prefs = await SharedPreferences.getInstance();
    final storedRefreshToken = prefs.getString(ApiConstants.refreshTokenKey);
    
    if (storedRefreshToken != null) {
      // Call backend logout with refreshToken as query parameter
      final response = await _httpService.delete<void>(
        '${ApiConstants.authLogout}?refreshToken=$storedRefreshToken',
        includeAuth: true,
      );
      
      // Clear stored tokens regardless of response
      await _httpService.clearTokens();
      
      if (response.success) {
        return ApiResponse.success(null, message: response.message ?? 'Logged out successfully');
      } else {
        return ApiResponse.error(response.error ?? 'Logout failed');
      }
    } else {
      // No refresh token, just clear local storage
      await _httpService.clearTokens();
      return ApiResponse.success(null, message: 'Logged out successfully');
    }
  }

  // Check if user is logged in
  Future<bool> isLoggedIn() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final accessToken = prefs.getString(ApiConstants.accessTokenKey);
      return accessToken != null && accessToken.isNotEmpty;
    } catch (e) {
      return false;
    }
  }

  // Get current user ID
  Future<String?> getCurrentUserId() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      return prefs.getString(ApiConstants.userIdKey);
    } catch (e) {
      return null;
    }
  }
}