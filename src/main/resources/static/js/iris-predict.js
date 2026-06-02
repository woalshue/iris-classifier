// 차트 객체를 전역변수로
// 나중에 '기존 차트 삭제 후 새로 그리기' 하려면 어디서든 접근 가능해야 하므로
let chart = null;

// 예제 버튼 클릭 시 실행
// HTML에서 버튼을 누르면 onclick="fillAndPredict(5.1, 3.5, 1.4, 0.2)" 이렇게 호출됨
// document.getElementById('sepalLength') -> HTML에서 id="sepalLength"인 input 요소를 찾아서 .value = sl로 값 채워넣음
// 4개 다 채운 뒤 바로 predict() 호출해서 자동으로 예측까지 실행함
function fillAndPredict(sl, sw, pl, pw) {
    document.getElementById('sepalLength').value = sl;
    document.getElementById('sepalWidth').value = sw;
    document.getElementById('petalLength').value = pl;
    document.getElementById('petalWidth').value = pw;
    predict();
}

// 서버에 예측 요청
// 입력값 수집
// 4개의 input에서 현재 값을 읽어서 객체로 묶음
// { sepalLength: "5.1", sepalWidth: "3.5", petalLength: "1.4", petalWidth: "0.2" } 이런 식으로
// async/await (비동기처리): 서버에 요청을 보내고 응답 올 때까지 기다려야 하는데, 그냥 기다리면 브라우저가 멈춰버림
// async/await를 쓰면 기다리는 동안 브라우저는 다른 작업을 계속 하고, 응답이오면 그때 다음 줄을 실행함
async function predict() {
    const data = {
        sepalLength: document.getElementById('sepalLength').value,
        sepalWidth: document.getElementById('sepalWidth').value,
        petalLength: document.getElementById('petalLength').value,
        petalWidth: document.getElementById('petalWidth').value
    };

    try {
        // Fetch API: fetch()를 통해 REST API를 호출
        // 데이터를 제이슨 문자열로 변환하여 보내고, 서버의 응답도 제이슨으로 받아서 처리
        const response = await fetch('./api/iris/predict', { // 요청 보낼 URL (컨트롤러 엔드포인트)
            method: 'POST',
            headers: {'Content-Type': 'application/json'}, // 보내는 데이터 제이슨임을 서버에 알림
            body: JSON.stringify(data) // JS 객체를 제이슨 문자열로 변환 {"sepalLength":5.1,...}
        });

        // 응답 처리
        const result = await response.json(); // 서버가 보낸 제이슨 문자열을 JS 객체로 변환
        // result 형태: { predictedSpecies: "Setosa", probabilities: { Setosa: 0.95, ... } }

        // 결과 텍스트 업데이트
        document.getElementById('result-area').style.display = 'block'; // 숨겨져 있던 결과 영역을 보이게 함
        document.getElementById('predictedSpecies').innerText = result.predictedSpecies; // 예측 품종명을 화면에 표시

        // 확률 데이터로 차트 그리기 호출
        renderChart(result.probabilities);

    } catch (error) {
        // 에러 처리: try-catch 로 네트워크 오류 등 예외 잡아서 alert로 사용자에게 알림
        console.error('Error:', error);
        alert('예측 중 오류가 발생했습니다.');
    }
}

// 차트 그리기
function renderChart(probabilities) {

    // probabilities가 { Setosa: 0.95, Versicolor: 0.03, Virginica: 0.02 } 라면,
    const ctx = document.getElementById('probabilityChart').getContext('2d');
    const labels = Object.keys(probabilities); // ["Setosa", "Versicolor", "Virginica"] (키만 추출)
    const values = Object.values(probabilities); // [0.95, 0.03, 0.02] (값만 추출)

    // 기존 차트가 있으면 삭제 후 새로 생성 (전역변수 chart에 이전 차트가 있으면 먼저 지움)
    // 예측을 여러 번 하면 차트를 여러 번 그리게 되는데, 이전 차트를 삭제하지 않으면 새로운 예측을 할 때마다 그래프가 겹쳐서 나타날 수 있으므로
    if (chart) {
        chart.destroy();
    }

    // Chart.js로 차트 생성
    // 그래프를 그림
    // probabilities 객체의 키(Setosa, Versicolor, Virginica)를 라벨로, 값(확률)을 데이터로 사용
    // 새로 생성한 차트 객체를 전역변수 chart에 저장해둬야 다음에 chart.destory()로 지울 수 있음
    chart = new Chart(ctx, {
        type: 'bar', // 막대 그래프
        data: {
            labels: labels, // X축 레이블 ["Setosa", ...]
            datasets: [{
                label: '품종별 확률',
                data: values, // 실제 확률값 [0.95, ...]
                backgroundColor: [ // 막대 채우기 색 (투명도 0.18)
                    'rgba(29, 158, 117, 0.18)',
                    'rgba(55, 138, 221, 0.18)',
                    'rgba(186, 117, 23, 0.18)'
                ],
                borderColor: [ // 막대 테두리 색
                    'rgba(29, 158, 117, 1)',
                    'rgba(55, 138, 221, 1)',
                    'rgba(186, 117, 23, 1)'
                ],
                borderWidth: 1.5,
                borderRadius: 6 // 막대 모서리 둥글게
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {display: false}
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 1, // Y축 0~1 고정
                    ticks: {font: {size: 12}, color: '#888'},
                    grid: {color: 'rgba(0,0,0,0.06)'}
                },
                x: {
                    ticks: {font: {size: 13}, color: '#444'},
                    grid: {display: false} // X축 그리드 숨김
                }
            }
        }
    });
}