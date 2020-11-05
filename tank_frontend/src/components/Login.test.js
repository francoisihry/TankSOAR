import '@testing-library/jest-dom'
import React from 'react'

import {rest} from 'msw'
import {setupServer} from 'msw/node'

import {render, fireEvent, screen, queryByAttribute } from '@testing-library/react'
// import Login from "./Login";
import App from "../App";
import '@testing-library/jest-dom/extend-expect'
import url from 'url'
import {BACKEND_URL, GET_TOKEN_URL} from "../services/BackendInterface";
import {MemoryRouter} from "react-router-dom";
import {getByPlaceholderText} from "@testing-library/dom";


// declare which API requests to mock
const server = setupServer(
    // capture "GET /greeting" requests
    rest.post(url.resolve(BACKEND_URL, GET_TOKEN_URL), (req, res, ctx) => {
        // respond using a mocked JSON body
        return res(ctx.json(null))
        // return res(ctx.json({ data: {access:'fake token'} }))
    })
)

// establish API mocking before all tests
beforeAll(() => server.listen())
// reset any request handlers that are declared as a part of our tests
// (i.e. for testing one-time error scenarios)
afterEach(() => server.resetHandlers())
// clean up once the tests are done
afterAll(() => server.close())

test('check redirecting to login url if not authenticated', async () => {
    render(<MemoryRouter initialEntries={["/"]}><App/></MemoryRouter>)
    expect(window.location.pathname).toEqual('/login')
})

test('check login', async () => {
    const {container} = render(<MemoryRouter initialEntries={["/"]}><App/></MemoryRouter>)

    const usernameInput = getByPlaceholderText(container, 'User Name')
    usernameInput.value = 'perceval'
    fireEvent.change(usernameInput)

    const passwordInput = getByPlaceholderText(container, 'Password')
    passwordInput.value = 'on3nAGr0s!'
    fireEvent.change(passwordInput)
    fireEvent.click(screen.getByText(/LOGIN/i))
})