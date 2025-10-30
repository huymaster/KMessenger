import 'dart:async';
import 'dart:developer';
import 'package:flutter/foundation.dart';
import '../models/api_response.dart';
import '../utils/api_constants.dart';
import 'auth_service.dart';
import 'http_service.dart';

class ApiInterceptor {
  static final ApiInterceptor _instance = ApiInterceptor._internal();
  factory ApiInterceptor() => _instance;
  ApiInterceptor._internal();

  final HttpService _httpService = HttpService();
  final AuthService _authService = AuthService();
  
  bool _isRefreshing = false;
  final List<Completer<String?>> _pendingRequests = [];

  // Handle token refresh automatically
  Future<String?> _handleTokenRefresh() async {
    if (_isRefreshing) {
      // If already refreshing, wait for it to complete
      final completer = Completer<String?>();
      _pendingRequests.add(completer);
      return completer.future;
    }

    _isRefreshing = true;
    
    try {
      final refreshResponse = await _authService.refreshToken();
      
      if (refreshResponse.success && refreshResponse.data != null) {
        final newToken = refreshResponse.data!.accessToken;
        
        // Complete all pending requests with the new token
        for (final completer in _pendingRequests) {
          completer.complete(newToken);
        }
        _pendingRequests.clear();
        
        return newToken;
      } else {
        // Refresh failed, clear tokens and redirect to login
        await _httpService.clearTokens();
        
        // Complete all pending requests with null (failed)
        for (final completer in _pendingRequests) {
          completer.complete(null);
        }
        _pendingRequests.clear();
        
        return null;
      }
    } catch (e) {
      log('Token refresh error: $e');
      await _httpService.clearTokens();
      
      // Complete all pending requests with null (failed)
      for (final completer in _pendingRequests) {
        completer.complete(null);
      }
      _pendingRequests.clear();
      
      return null;
    } finally {
      _isRefreshing = false;
    }
  }

  // Check if response indicates token expiry
  bool _isTokenExpired(ApiResponse response) {
    return !response.success && 
           (response.error?.contains('token') == true ||
            response.error?.contains('unauthorized') == true ||
            response.error?.contains('401') == true);
  }

  // Retry request with new token
  Future<ApiResponse<T>> _retryWithNewToken<T>(
    Future<ApiResponse<T>> Function() originalRequest,
  ) async {
    final newToken = await _handleTokenRefresh();
    
    if (newToken != null) {
      // Token refreshed successfully, retry original request
      return originalRequest();
    } else {
      // Token refresh failed
      return ApiResponse.error('Authentication failed. Please login again.');
    }
  }

  // Intercept and handle API requests
  Future<ApiResponse<T>> intercept<T>(
    Future<ApiResponse<T>> Function() request, {
    bool autoRetryOnTokenExpiry = true,
  }) async {
    try {
      final response = await request();
      
      // Check if token expired and auto-retry is enabled
      if (autoRetryOnTokenExpiry && _isTokenExpired(response)) {
        log('Token expired, attempting to refresh...');
        return _retryWithNewToken(request);
      }
      
      return response;
    } catch (e) {
      log('API Interceptor error: $e');
      return ApiResponse.error('Network error: $e');
    }
  }

  // Log API requests (for debugging)
  void logRequest(String method, String endpoint, dynamic body) {
    if (kDebugMode) {
      log('API Request: $method ${ApiConstants.baseUrl}$endpoint');
      if (body != null) {
        log('Body: $body');
      }
    }
  }

  // Log API responses (for debugging)
  void logResponse(String endpoint, ApiResponse response) {
    if (kDebugMode) {
      log('API Response: $endpoint - Success: ${response.success}');
      if (!response.success) {
        log('Error: ${response.error}');
      }
    }
  }
}

// Extension to make interceptor usage easier
extension HttpServiceInterceptor on HttpService {
  Future<ApiResponse<T>> getWithInterceptor<T>(
    String endpoint, {
    bool includeAuth = true,
    T Function(dynamic)? fromJson,
  }) async {
    return ApiInterceptor().intercept(() => get<T>(
      endpoint,
      includeAuth: includeAuth,
      fromJson: fromJson,
    ));
  }

  Future<ApiResponse<T>> postWithInterceptor<T>(
    String endpoint, {
    dynamic body,
    bool includeAuth = true,
    T Function(dynamic)? fromJson,
  }) async {
    ApiInterceptor().logRequest('POST', endpoint, body);
    
    final response = await ApiInterceptor().intercept(() => post<T>(
      endpoint,
      body: body,
      includeAuth: includeAuth,
      fromJson: fromJson,
    ));
    
    ApiInterceptor().logResponse(endpoint, response);
    return response;
  }

  Future<ApiResponse<T>> putWithInterceptor<T>(
    String endpoint, {
    dynamic body,
    bool includeAuth = true,
    T Function(dynamic)? fromJson,
  }) async {
    return ApiInterceptor().intercept(() => put<T>(
      endpoint,
      body: body,
      includeAuth: includeAuth,
      fromJson: fromJson,
    ));
  }

  Future<ApiResponse<T>> deleteWithInterceptor<T>(
    String endpoint, {
    bool includeAuth = true,
    T Function(dynamic)? fromJson,
  }) async {
    return ApiInterceptor().intercept(() => delete<T>(
      endpoint,
      includeAuth: includeAuth,
      fromJson: fromJson,
    ));
  }
}