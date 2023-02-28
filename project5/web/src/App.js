import "./App.css"
import {
    IconBoxSeam,
    IconUser,
    IconCheckupList,
    IconLogout,
} from "@tabler/icons"
import {
    ThemeIcon,
    UnstyledButton,
    Button,
    Group,
    Text,
    Box,
    Header,
    MantineProvider,
    TextInput,
    Flex,
} from "@mantine/core"
import React, { useState } from "react"
import CustomerView from "./CustomerView"
import AdminView from "./AdminView"
import DeliveryView from "./DeliveryView"

function MainLink({ icon, color, label, selected, onClick }) {
    return (
        <UnstyledButton
            sx={(theme) => ({
                display: "block",
                padding: theme.spacing.xs,
                borderRadius: theme.radius.sm,
                color: theme.black,
                backgroundColor: selected ? theme.colors[color][2] : "",
                "&:hover": {
                    backgroundColor: selected
                        ? theme.colors[color][2]
                        : theme.colors.gray[2],
                },
            })}
            onClick={onClick}
        >
            <Group>
                <ThemeIcon color={color} variant="light">
                    {icon}
                </ThemeIcon>

                <Text size="sm">{label}</Text>
            </Group>
        </UnstyledButton>
    )
}
function App() {
    const [view, setView] = useState("customer")
    const [loggedIn, setLoggedIn] = useState(false)
    const [email, setEmail] = useState("")
    const [name, setName] = useState("")
    const [address, setAddress] = useState("")

    if (!loggedIn) {
        return (
            <MantineProvider withGlobalStyles withNormalizeCSS>
                <Box
                    sx={(theme) => ({
                        height: "100vh",
                        width: "100vw",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                    })}
                >
                    <Box
                        sx={(theme) => ({
                            height: "250px",
                            width: "412px",
                            background: theme.colors.blue[1],
                            borderRadius: "8px",
                        })}
                    >
                        <Flex key={"email-input"} style={{ marginTop: "16px" }}>
                            <Text
                                size="lg"
                                style={{ width: "80px", marginLeft: "16px" }}
                            >
                                Email:
                            </Text>
                            <TextInput
                                style={{ width: "300px" }}
                                value={email}
                                onChange={(e) => {
                                    setEmail(e.target.value)
                                }}
                            ></TextInput>
                        </Flex>
                        <Flex key={"name-input"} style={{ marginTop: "8px" }}>
                            <Text
                                size="lg"
                                style={{ width: "80px", marginLeft: "16px" }}
                            >
                                Name:
                            </Text>
                            <TextInput
                                style={{ width: "300px" }}
                                value={name}
                                onChange={(e) => {
                                    setName(e.target.value)
                                }}
                            ></TextInput>
                        </Flex>
                        <Flex
                            key={"address-input"}
                            style={{ marginTop: "8px" }}
                        >
                            <Text
                                size="lg"
                                style={{ width: "80px", marginLeft: "16px" }}
                            >
                                Address:
                            </Text>
                            <TextInput
                                style={{ width: "300px" }}
                                value={address}
                                onChange={(e) => {
                                    setAddress(e.target.value)
                                }}
                            ></TextInput>
                        </Flex>

                        <Button
                            sx={(theme) => ({
                                display: "block",
                                width: "calc(100% - 32px)",
                                margin: "16px",
                                marginTop: "64px",
                            })}
                            onClick={() => {
                                fetch("http://localhost:8001/users", {
                                    method: "POST",
                                    body: `${email}\n${name}\n${address}`,
                                }).then(() => {
                                    setLoggedIn(true)
                                })
                            }}
                        >
                            <Group>
                                <Text size="sm">REGISTER</Text>
                            </Group>
                        </Button>
                    </Box>
                </Box>
            </MantineProvider>
        )
    }

    let mainBody
    switch (view) {
        case "customer":
            mainBody = <CustomerView email={email} />
            break
        case "admin":
            mainBody = <AdminView />
            break
        case "delivery":
            mainBody = <DeliveryView />
            break
    }

    return (
        <MantineProvider withGlobalStyles withNormalizeCSS>
            <Header p="xs">
                <Group position="center">
                    <MainLink
                        icon={<IconUser size={16} />}
                        color="green"
                        label="Customer"
                        key="customer"
                        selected={view === "customer"}
                        onClick={() => setView("customer")}
                    />
                    <MainLink
                        icon={<IconCheckupList size={16} />}
                        color="blue"
                        label="Admin"
                        key="admin"
                        selected={view === "admin"}
                        onClick={() => setView("admin")}
                    />
                    <MainLink
                        icon={<IconBoxSeam size={16} />}
                        color="orange"
                        label="Delivery"
                        key="delivery"
                        selected={view === "delivery"}
                        onClick={() => setView("delivery")}
                    />
                    <MainLink
                        icon={<IconLogout size={16} />}
                        color="red"
                        label="Log Out"
                        key="logout"
                        selected={false}
                        onClick={() => setLoggedIn(false)}
                    />
                </Group>
            </Header>
            <Box
                sx={(theme) => ({
                    background: theme.colors.gray[0],
                    paddingTop: "16px",
                    height: "calc(100vh - 87px)",
                })}
            >
                {mainBody}
            </Box>
        </MantineProvider>
    )
}

export default App
