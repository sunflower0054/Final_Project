(function(window, document) {
    'use strict';

    const DEFAULT_HEADERS = {
        'X-Requested-With': 'XMLHttpRequest'
    };
    const STATE_CHANGING_METHODS = ['POST', 'PUT', 'PATCH', 'DELETE'];

    function getMetaContent(name) {
        return document.querySelector(`meta[name="${name}"]`)?.content || null;
    }

    function getCsrfHeaders(method) {
        const normalizedMethod = (method || 'GET').toUpperCase();
        if (!STATE_CHANGING_METHODS.includes(normalizedMethod)) {
            return {};
        }

        const csrfToken = getMetaContent('_csrf');
        const csrfHeader = getMetaContent('_csrf_header');

        if (!csrfToken || !csrfHeader) {
            return {};
        }

        return {
            [csrfHeader]: csrfToken
        };
    }

    async function parseResponse(response) {
        if (response.status === 204 || response.status === 205) {
            return null;
        }

        const contentType = (response.headers.get('content-type') || '').toLowerCase();

        if (contentType.includes('application/json')) {
            try {
                return await response.json();
            } catch (error) {
                return null;
            }
        }

        const text = await response.text();
        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch (error) {
            return text;
        }
    }

    function createHttpError(response, data) {
        const fallbackMessage = `요청에 실패했습니다. (${response.status})`;
        const message = (data && typeof data === 'object' && data.message)
            ? data.message
            : (typeof data === 'string' && data.trim() ? data : fallbackMessage);

        const error = new Error(message);
        error.status = response.status;
        error.statusText = response.statusText;
        error.data = data;
        error.response = response;
        return error;
    }

    async function request(url, options) {
        const requestOptions = options || {};
        const method = (requestOptions.method || 'GET').toUpperCase();
        const redirectOn401 = requestOptions.redirectOn401 !== false;

        const headers = {
            ...DEFAULT_HEADERS,
            ...(requestOptions.headers || {}),
            ...getCsrfHeaders(method)
        };

        let body = requestOptions.body;
        if (body !== undefined && body !== null && !(body instanceof FormData)) {
            const isPlainObject = typeof body === 'object';
            if (isPlainObject) {
                body = JSON.stringify(body);
            }

            if (!headers['Content-Type']) {
                headers['Content-Type'] = 'application/json';
            }
        }

        const response = await fetch(url, {
            ...requestOptions,
            method,
            headers,
            body,
            credentials: requestOptions.credentials || 'same-origin'
        });

        const data = await parseResponse(response);

        if (!response.ok) {
            if (response.status === 401 && redirectOn401) {
                window.location.href = '/member/login';
            }
            throw createHttpError(response, data);
        }

        return data;
    }

    function get(url, options) {
        return request(url, {
            ...(options || {}),
            method: 'GET'
        });
    }

    function post(url, body, options) {
        return request(url, {
            ...(options || {}),
            method: 'POST',
            body
        });
    }

    function put(url, body, options) {
        return request(url, {
            ...(options || {}),
            method: 'PUT',
            body
        });
    }

    function del(url, options) {
        return request(url, {
            ...(options || {}),
            method: 'DELETE'
        });
    }

    window.AppApi = {
        request,
        get,
        post,
        put,
        del,
        getCsrfHeaders,
        parseResponse
    };
})(window, document);
