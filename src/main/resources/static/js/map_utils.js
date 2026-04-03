// 파일명: map-utils.js

// 1. 지도 초기화 및 거주자 마커(빨간색) 찍기
function initMapAndHospitals(targetAddress, targetName) {
    kakao.maps.load(function () {
        var mapContainer = document.getElementById('map');
        var mapOption = { center: new kakao.maps.LatLng(37.566826, 126.978656), level: 5 };
        var map = new kakao.maps.Map(mapContainer, mapOption);
        var geocoder = new kakao.maps.services.Geocoder();

        if (targetAddress) {
            geocoder.addressSearch(targetAddress, function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    var coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                    map.setCenter(coords);

                    // 거주자 마커 (빨간색)
                    var imageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png';
                    var imageSize = new kakao.maps.Size(31, 35);
                    var markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize);

                    var marker = new kakao.maps.Marker({
                        position: coords,
                        map: map,
                        image: markerImage
                    });

                    var iw = new kakao.maps.InfoWindow({
                        content: `<div style="padding:6px 12px; font-size:13px; font-weight:700; color:#DC2626; border-radius:6px;">${targetName}님 사고 위치</div>`
                    });
                    iw.open(map, marker);

                    // 주변 응급실 검색 실행
                    searchHospitals(coords, map);
                } else {
                    document.getElementById('hospital-list').innerHTML = '<li style="padding:20px; text-align:center; color:var(--error);">주소를 찾을 수 없습니다.</li>';
                }
            });
        }
    });
}

// 2. 주변 응급실 검색 및 리스트 & 마커 생성
function searchHospitals(center, map) {
    var ps = new kakao.maps.services.Places();
    var hospitalListEl = document.getElementById('hospital-list');

    ps.keywordSearch('응급실', function(data, status) {
        if (status === kakao.maps.services.Status.OK) {
            hospitalListEl.innerHTML = '';

            var filteredData = data.filter(function(place) {
                var name = place.place_name;
                return !name.includes('동물') &&
                       !name.includes('요양') &&
                       !name.includes('떡볶이') &&
                       !name.includes('한의원') &&
                       !name.includes('청소') &&
                       !name.includes('치과');
            });

            if (filteredData.length === 0) {
                hospitalListEl.innerHTML = '<li style="padding:20px; text-align:center; color:var(--error);">조건에 맞는 주변 응급실을 찾을 수 없습니다.</li>';
                return;
            }

            var limit = Math.min(filteredData.length, 5);

            for (var i = 0; i < limit; i++) {
                var place = filteredData[i];

                // 병원 마커 표시 (기본 파란색)
                var hospitalPos = new kakao.maps.LatLng(place.y, place.x);
                var hospitalMarker = new kakao.maps.Marker({
                    position: hospitalPos,
                    map: map
                });

                var infowindow = new kakao.maps.InfoWindow({
                    content: `<div style="padding:5px; font-size:12px; font-weight:bold; color:#333;">${place.place_name}</div>`
                });

                kakao.maps.event.addListener(hospitalMarker, 'mouseover', (function(marker, infowindow) {
                    return function() { infowindow.open(map, marker); };
                })(hospitalMarker, infowindow));

                kakao.maps.event.addListener(hospitalMarker, 'mouseout', (function(infowindow) {
                    return function() { infowindow.close(); };
                })(infowindow));

                // 병원 리스트 생성
                var distanceText = place.distance ? (place.distance / 1000).toFixed(1) + 'km' : '-';
                var phoneText = place.phone || '전화번호 없음';
                var phoneLink = place.phone ? 'tel:' + place.phone.replace(/-/g, '') : '#';

                var li = document.createElement('li');
                li.className = 'hospital-item';
                li.innerHTML = `
                    <div class="h-info">
                        <div class="h-name">${place.place_name} <span class="h-dist">${distanceText}</span></div>
                        <div class="h-tel">${place.road_address_name || place.address_name}</div>
                    </div>
                    <a href="${phoneLink}" class="h-call-btn">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
                        ${phoneText}
                    </a>
                `;
                hospitalListEl.appendChild(li);
            }
        } else {
            hospitalListEl.innerHTML = '<li style="padding:20px; text-align:center; color:var(--error);">주변 응급실 정보를 찾을 수 없습니다.</li>';
        }
    }, {
        location: center,
        radius: 5000,
        sort: kakao.maps.services.SortBy.DISTANCE,
    });
}